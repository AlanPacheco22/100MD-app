const AudioManager = {
    ctx: null,
    enabled: true,

    init() {
        if (this.ctx) return;
        this.ctx = new (window.AudioContext || window.webkitAudioContext)();
    },

    ensureContext() {
        if (!this.ctx) this.init();
        if (this.ctx.state === 'suspended') {
            this.ctx.resume();
        }
    },

    toggle() {
        this.enabled = !this.enabled;
        return this.enabled;
    },

    // Correct answer: ascending major chord (C-E-G)
    playCorrect() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([523.25, 659.25, 783.99], 0.12, 0.3);
    },

    // Wrong answer: descending minor
    playWrong() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([311.13, 233.08], 0.15, 0.4);
    },

    // Reveal: short ding
    playReveal() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([880], 0.05, 0.15);
    },

    // Strike (X): deep impact
    playStrike() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([110], 0.2, 0.2);
        setTimeout(() => this.playTone([82.41], 0.15, 0.15), 100);
    },

    // Celebration: ascending scale
    playCelebration() {
        if (!this.enabled) return;
        this.ensureContext();
        const notes = [523, 587, 659, 698, 784, 880, 988, 1047];
        notes.forEach((freq, i) => {
            setTimeout(() => this.playTone([freq], 0.08, 0.15), i * 100);
        });
    },

    // Round start: fanfare
    playRoundStart() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([440, 554, 659], 0.08, 0.4);
    },

    // Round end: descending
    playRoundEnd() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([659, 554, 440], 0.08, 0.4);
    },

    // Steal attempt alert
    playStealAlert() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([660, 880, 660, 880], 0.1, 0.15);
    },

    // Score tick (for counting up)
    playScoreTick() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([1200], 0.02, 0.05);
    },

    // Button click feedback
    playClick() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([600], 0.02, 0.05);
    },

    // Core tone player
    playTone(frequencies, volume, duration) {
        if (!this.ctx) return;
        frequencies.forEach((freq, i) => {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.connect(gain);
            gain.connect(this.ctx.destination);
            osc.frequency.value = freq;
            osc.type = 'sine';
            const startTime = this.ctx.currentTime + (i * 0.02);
            gain.gain.setValueAtTime(volume, startTime);
            gain.gain.exponentialRampToValueAtTime(0.001, startTime + duration);
            osc.start(startTime);
            osc.stop(startTime + duration);
        });
    }
};
