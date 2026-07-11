function fastMoneyMixin() {
    return {
        fastMoneyPhase: 'select',
        fastMoneyPlayer1Id: null,
        fastMoneyPlayer2Id: null,
        fastMoneyAnswers: ['', '', '', '', ''],
        fastMoneyPlayer2Answers: ['', '', '', '', ''],
        fastMoneyPlayer1Time: 15,
        fastMoneyPlayer2Time: 20,
        fastMoneyPlayer1Results: null,
        fastMoneyPlayer2Results: null,
        fastMoneyTotal: 0,
        fastMoneyWon: false,
        fastMoneyQuestions: [],
        fastMoneyTimerInterval: null,
        fastMoneyPlayer1TotalPoints: 0,
        fastMoneyPlayer2TotalPoints: 0,

        async startFastMoney() {
            if (!this.fastMoneyPlayer1Id || !this.fastMoneyPlayer2Id) return;
            if (this.fastMoneyPlayer1Id === this.fastMoneyPlayer2Id) return;
            this.loading = true;
            this.fastMoneyPhase = 'player1';
            this.fastMoneyAnswers = ['', '', '', '', ''];
            this.fastMoneyPlayer2Answers = ['', '', '', '', ''];
            this.fastMoneyPlayer1Results = null;
            this.fastMoneyPlayer2Results = null;
            this.fastMoneyTotal = 0;
            try {
                const res = await fetch('/api/games/' + this.gameId + '/fast-money/start', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ player1Id: Number(this.fastMoneyPlayer1Id), player2Id: Number(this.fastMoneyPlayer2Id) })
                });
                if (res.ok) {
                    const data = await res.json();
                    this.fastMoneyQuestions = data.questions || [];
                    this.status = 'FAST_MONEY';
                    AudioManager.playFastMoneyStart();
                    this.startFastMoneyTimer(1);
                }
            } catch (e) {
                console.error('[startFastMoney] Error:', e);
            }
            this.loading = false;
        },

        async submitFastMoneyPlayer1() {
            this.loading = true;
            try {
                const res = await fetch('/api/games/' + this.gameId + '/fast-money/submit', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ participantId: Number(this.fastMoneyPlayer1Id), answers: this.fastMoneyAnswers.filter(a => a.trim()) })
                });
                if (res.ok) {
                    this.stopFastMoneyTimer();
                    this.fastMoneyPhase = 'player2';
                    AudioManager.playFastMoneyDing();
                    this.startFastMoneyTimer(2);
                }
            } catch (e) {
                console.error('[submitFastMoneyPlayer1] Error:', e);
            }
            this.loading = false;
        },

        async submitFastMoneyPlayer2() {
            this.loading = true;
            try {
                const res = await fetch('/api/games/' + this.gameId + '/fast-money/submit', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ participantId: Number(this.fastMoneyPlayer2Id), answers: this.fastMoneyPlayer2Answers.filter(a => a.trim()) })
                });
                if (res.ok) {
                    this.stopFastMoneyTimer();
                    const statusRes = await fetch('/api/games/' + this.gameId + '/fast-money/status');
                    if (statusRes.ok) {
                        const statusData = await statusRes.json();
                        this.fastMoneyPlayer1Results = statusData.player1Answers || [];
                        this.fastMoneyPlayer2Results = statusData.player2Answers || [];
                        this.fastMoneyTotal = statusData.combinedTotal || 0;
                        this.fastMoneyWon = statusData.wonBonus || false;
                        this.fastMoneyPlayer1TotalPoints = statusData.player1TotalPoints || 0;
                        this.fastMoneyPlayer2TotalPoints = statusData.player2TotalPoints || 0;
                    }
                    this.fastMoneyPhase = 'results';
                    AudioManager.playFastMoneyEnd();
                }
            } catch (e) {
                console.error('[submitFastMoneyPlayer2] Error:', e);
            }
            this.loading = false;
        },

        startFastMoneyTimer(player) {
            this.stopFastMoneyTimer();
            if (player === 1) {
                this.fastMoneyPlayer1Time = 15;
                this.fastMoneyTimerInterval = setInterval(() => {
                    this.fastMoneyPlayer1Time--;
                    if (this.fastMoneyPlayer1Time <= 5 && this.fastMoneyPlayer1Time > 0) {
                        AudioManager.playTimerWarning();
                    }
                    if (this.fastMoneyPlayer1Time <= 0) {
                        this.stopFastMoneyTimer();
                        AudioManager.playTimerEnd();
                        this.submitFastMoneyPlayer1();
                    }
                }, 1000);
            } else {
                this.fastMoneyPlayer2Time = 20;
                this.fastMoneyTimerInterval = setInterval(() => {
                    this.fastMoneyPlayer2Time--;
                    if (this.fastMoneyPlayer2Time <= 5 && this.fastMoneyPlayer2Time > 0) {
                        AudioManager.playTimerWarning();
                    }
                    if (this.fastMoneyPlayer2Time <= 0) {
                        this.stopFastMoneyTimer();
                        AudioManager.playTimerEnd();
                        this.submitFastMoneyPlayer2();
                    }
                }, 1000);
            }
        },

        stopFastMoneyTimer() {
            if (this.fastMoneyTimerInterval) {
                clearInterval(this.fastMoneyTimerInterval);
                this.fastMoneyTimerInterval = null;
            }
        }
    };
}
