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

    // Face-off buzzer: sharp electronic buzz
    playBuzzer() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([180, 220, 180], 0.15, 0.15);
        setTimeout(() => this.playTone([250, 300, 250], 0.12, 0.1), 80);
    },

    // Timer tick: subtle click each second
    playTick() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([900], 0.03, 0.04);
    },

    // Timer warning: urgent triple tick (last 3 seconds)
    playTimerWarning() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([1000], 0.06, 0.06);
        setTimeout(() => this.playTone([1000], 0.06, 0.06), 80);
        setTimeout(() => this.playTone([1000], 0.06, 0.06), 160);
    },

    // Timer end: buzzer sound
    playTimerEnd() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([200, 150], 0.18, 0.3);
        setTimeout(() => this.playTone([150, 100], 0.12, 0.2), 150);
    },

    // Fast money start: exciting fanfare
    playFastMoneyStart() {
        if (!this.enabled) return;
        this.ensureContext();
        const notes = [523, 659, 784, 1047];
        notes.forEach((freq, i) => {
            setTimeout(() => this.playTone([freq], 0.1, 0.2), i * 120);
        });
    },

    // Fast money answer ding: positive chime
    playFastMoneyDing() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([880, 1109], 0.08, 0.15);
    },

    // Fast money end: results fanfare
    playFastMoneyEnd() {
        if (!this.enabled) return;
        this.ensureContext();
        const notes = [440, 554, 659, 880, 1047];
        notes.forEach((freq, i) => {
            setTimeout(() => this.playTone([freq], 0.08, 0.25), i * 150);
        });
    },

    // Sudden death: dramatic tension sound
    playSuddenDeath() {
        if (!this.enabled) return;
        this.ensureContext();
        this.playTone([110], 0.15, 0.4);
        setTimeout(() => this.playTone([130], 0.12, 0.3), 200);
        setTimeout(() => this.playTone([165], 0.1, 0.5), 400);
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
