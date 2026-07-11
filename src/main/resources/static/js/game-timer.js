function gameTimerMixin() {
    return {
        timerEnabled: true,
        turnTimeLimit: 10,
        timerRemaining: 0,
        timerInterval: null,
        timerRunning: false,

        startTimer() {
            this.stopTimer();
            if (!this.timerEnabled) return;
            this.timerRemaining = this.turnTimeLimit;
            this.timerRunning = true;
            this.timerInterval = setInterval(() => {
                this.timerRemaining--;
                if (this.timerRemaining <= 3 && this.timerRemaining > 0) {
                    AudioManager.playTimerWarning();
                } else if (this.timerRemaining > 0) {
                    AudioManager.playTick();
                }
                if (this.timerRemaining <= 0) {
                    this.stopTimer();
                    AudioManager.playTimerEnd();
                    this.incrementError();
                }
            }, 1000);
        },

        stopTimer() {
            if (this.timerInterval) {
                clearInterval(this.timerInterval);
                this.timerInterval = null;
            }
            this.timerRunning = false;
            this.timerRemaining = 0;
        },

        get timerProgress() {
            if (!this.timerEnabled || this.turnTimeLimit <= 0) return 0;
            return (this.timerRemaining / this.turnTimeLimit) * 100;
        },

        get timerStrokeDashoffset() {
            const circumference = 2 * Math.PI * 35;
            return circumference - (this.timerProgress / 100) * circumference;
        },

        get timerColorClass() {
            if (this.timerRemaining <= 3) return 'timer-critical';
            if (this.timerRemaining <= 5) return 'timer-warning';
            return '';
        }
    };
}
