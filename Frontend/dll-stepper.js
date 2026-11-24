/* ==========================================================================
   dll-stepper.js  -  MEMBER 3
   Drives the Play / Pause / Next / Prev bar, the Algorithm box and the
   step description. It does NOT touch the Insert / Delete / Traverse buttons,
   so it cannot clash with Member 4's code.

   RIGHT NOW it plays a built-in demo ("Insert at Tail") so the animations
   can be shown without a backend.

   MEMBER 4 - HOW TO HOOK UP THE REAL BACKEND STEPS
   -----------------------------------------------
   Call this after you receive the steps from the API:

     DllStepper.load({
       title: 'Insert at Tail',
       lines: ['newNode <- ...', 'tail.next <- newNode', ...],  // algorithm lines
       steps: [ { line: 0, text: 'Create the node' }, ... ],     // line = index in lines
       onStep: function (index, step, forward) {
         // draw the list as it looks AFTER this step (index = -1 means "before step 1")
       }
     });

   Controls available: DllStepper.play() / pause() / next() / prev() / reset().
   ========================================================================== */
(function () {
  'use strict';

  var $ = function (id) { return document.getElementById(id); };
  var canvas = $('dll-canvas');
  var pool = $('avail-list');
  var linesEl = $('algo-lines');
  var playBtn = $('btn-play');
  if (!canvas || !pool || !linesEl || !playBtn) { return; }

  var el = {
    title: $('algo-title'), desc: $('step-description'), badge: $('step-badge'),
    counter: $('step-counter'), bar: $('step-progress'),
    prev: $('btn-step-prev'), next: $('btn-step-next'), reset: $('btn-step-reset'),
    speed: $('speed-select'), size: $('list-size'), head: $('head-id'),
    tail: $('tail-id'), avail: $('avail-count')
  };

  var cfg = null;
  var snaps = [];
  var idx = -1;
  var timer = null;
  var playing = false;

  /* ---------- HTML builders (same structure as Member 2's nodes) ---------- */
  var CONN = '<div class="connector-slot" aria-hidden="true">' +
    '<svg class="dll-arrows" viewBox="0 0 44 28" xmlns="http://www.w3.org/2000/svg" focusable="false">' +
    '<g class="arrow arrow-next"><line class="arrow-line" x1="2" y1="9" x2="35" y2="9" pathLength="1"/>' +
    '<polygon class="arrow-head" points="35,4.5 43,9 35,13.5"/></g>' +
    '<g class="arrow arrow-prev"><line class="arrow-line" x1="42" y1="19" x2="9" y2="19" pathLength="1"/>' +
    '<polygon class="arrow-head" points="9,14.5 1,19 9,23.5"/></g></svg></div>';

  function cell(kind, label, value) {
    var nul = value === 'NULL' ? ' is-null' : '';
    return '<div class="node-cell node-' + kind + '"><span class="cell-label">' + label +
      '</span><span class="cell-value' + nul + '">' + value + '</span></div>';
  }

  function nodeHTML(id, data, prev, next) {
    return '<div class="node-unit" data-node-id="' + id + '"><div class="pointer-badges"></div>' +
      '<div class="node-box">' + cell('prev', 'PREV', prev) + cell('data', 'ID #' + id, data) +
      cell('next', 'NEXT', next) + '</div></div>';
  }

  var node = function (root, id) { return root.querySelector('[data-node-id="' + id + '"]'); };

  function setPointer(nodeEl, kind, value) {
    var v = nodeEl.querySelector('.node-' + kind + ' .cell-value');
    v.textContent = value;
    v.classList.toggle('is-null', value === 'NULL');
  }

  function flash(nodeEl, kind) {
    if (nodeEl) { nodeEl.querySelector('.node-' + kind).classList.add('ptr-changed'); }
  }

  /* ---------- Built-in demo: Insert at Tail (value 90) ---------- */
  function demoConfig() {
    return {
      title: 'Insert at Tail',
      lines: [
        'newNode \u2190 take a free node from AVAIL',
        'if head = NULL: head \u2190 tail \u2190 newNode',
        'newNode.prev \u2190 tail',
        'tail.next \u2190 newNode',
        'tail \u2190 newNode'
      ],
      steps: [
        {
          line: 0, text: 'Take a free node (#4) from the AVAIL pool and store the value 90 in it.',
          meta: { avail: 4 },
          mutate: function (c, a) {
            a.querySelector('[data-avail-id="4"]').classList.add('is-taken');
            c.querySelector('#tail-null').insertAdjacentHTML('beforebegin', nodeHTML(4, 90, 'NULL', 'NULL'));
          },
          enter: function (c) { return [node(c, 4)]; },
          focus: function (c, a) { a.querySelector('[data-avail-id="4"]').classList.add('avail-pulse'); }
        },
        {
          line: 1, text: 'Check: head is not NULL, so the list already has nodes. Skip the empty-list case.',
          focus: function (c) { node(c, 1).classList.add('node-current'); }
        },
        {
          line: 2, text: 'Set newNode.prev = tail. The new node now points back to node #3.',
          mutate: function (c) { setPointer(node(c, 4), 'prev', '#3'); },
          focus: function (c) { node(c, 4).classList.add('node-current'); flash(node(c, 4), 'prev'); }
        },
        {
          line: 3, text: 'Set tail.next = newNode. Node #3 now points forward to the new node, and the arrows appear.',
          mutate: function (c) {
            setPointer(node(c, 3), 'next', '#4');
            node(c, 4).insertAdjacentHTML('beforebegin', CONN);
          },
          enter: function (c) { return [node(c, 4).previousElementSibling]; },
          focus: function (c) { node(c, 3).classList.add('node-current'); flash(node(c, 3), 'next'); }
        },
        {
          line: 4, text: 'Move tail to newNode. The TAIL badge jumps to node #4 and the list size becomes 4.',
          meta: { size: 4, tail: 4 },
          mutate: function (c) {
            var old = node(c, 3).querySelector('.badge-tail');
            if (old) { old.remove(); }
            node(c, 4).querySelector('.pointer-badges').innerHTML = '<span class="badge badge-tail">TAIL</span>';
          },
          focus: function (c) {
            node(c, 4).classList.add('node-found');
            c.querySelector('#tail-null').classList.add('is-active');
          }
        }
      ]
    };
  }

  /* ---------- Snapshots (so Prev can go back without replaying animations) ---------- */
  function cleanAnimClasses(root) {
    var list = root.querySelectorAll('.node-enter,.is-new');
    for (var i = 0; i < list.length; i++) {
      list[i].classList.remove('node-enter', 'is-new', 'from-left', 'from-right');
    }
  }

  function takeSnap(c, a, meta) {
    cleanAnimClasses(c);
    return { c: c.innerHTML, a: a.innerHTML, meta: Object.assign({}, meta) };
  }

  function buildSnaps(config) {
    var work = document.createElement('div');
    work.innerHTML = '<div class="c"></div><div class="a"></div>';
    var c = work.querySelector('.c');
    var a = work.querySelector('.a');
    c.innerHTML = canvas.innerHTML;
    a.innerHTML = pool.innerHTML;
    var meta = {
      size: Number(el.size.textContent) || 0,
      head: el.head.textContent,
      tail: el.tail.textContent,
      avail: Number(el.avail.textContent) || 0
    };
    var out = [takeSnap(c, a, meta)];
    config.steps.forEach(function (s) {
      if (s.mutate) { s.mutate(c, a); }
      meta = Object.assign({}, meta, s.meta || {});
      out.push(takeSnap(c, a, meta));
    });
    return out;
  }

  /* ---------- Rendering ---------- */
  function setPill(span, value, animate) {
    var text = String(value);
    if (span.textContent !== text) {
      span.textContent = text;
      if (animate) {
        var pill = span.parentElement;
        pill.classList.remove('pill-updated');
        void pill.offsetWidth;
        pill.classList.add('pill-updated');
      }
    }
  }

  function renderDemo(i, forward) {
    var snap = snaps[i + 1];
    var step = i >= 0 ? cfg.steps[i] : null;
    canvas.innerHTML = snap.c;
    pool.innerHTML = snap.a;
    setPill(el.size, snap.meta.size, forward);
    setPill(el.head, snap.meta.head, forward);
    setPill(el.tail, snap.meta.tail, forward);
    el.avail.textContent = snap.meta.avail;
    if (step && forward && step.enter) {
      step.enter(canvas).forEach(function (n) {
        n.classList.add(n.classList.contains('connector-slot') ? 'is-new' : 'node-enter');
      });
    }
    if (step && step.focus) { step.focus(canvas, pool); }
    /* keep the node being worked on in view when the list is wider than the canvas */
    var target = canvas.querySelector('.node-current, .node-found');
    if (target && target.scrollIntoView) {
      target.scrollIntoView({ inline: 'center', block: 'nearest', behavior: 'smooth' });
    }
  }

  function updateUI() {
    var last = cfg.steps.length - 1;
    var step = idx >= 0 ? cfg.steps[idx] : null;

    var items = linesEl.children;
    for (var k = 0; k < items.length; k++) {
      items[k].classList.toggle('is-active', !!step && k === step.line);
      items[k].classList.toggle('is-done', !!step && k < step.line);
    }

    el.counter.textContent = idx < 0 ? 'Ready' : 'Step ' + (idx + 1) + ' / ' + cfg.steps.length;
    el.bar.style.width = (cfg.steps.length ? ((idx + 1) / cfg.steps.length) * 100 : 0) + '%';

    el.desc.textContent = step ? step.text : 'Press Play or Next to watch ' + cfg.title + ' step by step.';
    el.desc.classList.remove('step-swap');
    void el.desc.offsetWidth;
    el.desc.classList.add('step-swap');

    var state = idx < 0 ? 'is-info' : (idx === last ? 'is-success' : (playing ? 'is-busy' : 'is-info'));
    el.badge.className = 'step-badge ' + state;
    el.badge.textContent = idx < 0 ? 'READY' : (idx === last ? 'DONE' : 'STEP ' + (idx + 1) + '/' + cfg.steps.length);

    el.prev.disabled = idx < 0;
    el.next.disabled = idx >= last;
    el.reset.disabled = idx < 0 && !playing;
    playBtn.classList.toggle('is-playing', playing);
    playBtn.setAttribute('aria-pressed', String(playing));
    playBtn.innerHTML = playing ? '&#9208; Pause' : '&#9654; Play';
  }

  function go(i, forward) {
    idx = i;
    if (cfg.onStep) { cfg.onStep(i, i >= 0 ? cfg.steps[i] : null, forward); } else { renderDemo(i, forward); }
    updateUI();
  }

  /* ---------- Controls ---------- */
  function delay() { return 1700 / (Number(el.speed.value) || 1); }

  function stopTimer() { if (timer) { clearInterval(timer); timer = null; } }

  function pause() {
    stopTimer();
    playing = false;
    if (cfg) { updateUI(); } /* cfg is empty while load() runs for the first time */
  }

  function tick() {
    var last = cfg.steps.length - 1;
    if (idx < last) { go(idx + 1, true); }
    if (idx >= last) { pause(); }
  }

  function play() {
    if (!cfg.steps.length) { return; }
    stopTimer();
    if (idx >= cfg.steps.length - 1) { go(-1, false); }
    playing = true;
    tick();
    if (playing) { timer = setInterval(tick, delay()); }
  }

  function next() { pause(); if (idx < cfg.steps.length - 1) { go(idx + 1, true); } }
  function prev() { pause(); if (idx >= 0) { go(idx - 1, false); } }
  function reset() { pause(); go(-1, false); }

  playBtn.addEventListener('click', function () { if (playing) { pause(); } else { play(); } });
  el.next.addEventListener('click', next);
  el.prev.addEventListener('click', prev);
  el.reset.addEventListener('click', reset);
  el.speed.addEventListener('change', function () {
    if (playing) { stopTimer(); timer = setInterval(tick, delay()); }
  });

  /* ---------- Public API ---------- */
  function load(config) {
    pause();
    cfg = config;
    idx = -1;
    el.title.textContent = config.title || '';
    linesEl.innerHTML = '';
    config.lines.forEach(function (text) {
      var li = document.createElement('li');
      li.textContent = text;
      linesEl.appendChild(li);
    });
    snaps = config.onStep ? [] : buildSnaps(config);
    updateUI();
  }

  window.DllStepper = { load: load, play: play, pause: pause, next: next, prev: prev, reset: reset };

  load(demoConfig());
})();
