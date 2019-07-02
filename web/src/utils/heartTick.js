export const heartTickMessage = {
    timeout: 5000,
    timeoutObj: null,
    serverTimeoutObj: null,
    reset: function(ws, token) {
      // console.log('正在重置心跳')
      clearTimeout(this.timeoutObj);
      clearTimeout(this.serverTimeoutObj);
      this.start(ws, token);
    },
    start: function(ws, token) {
      var self = this;
      this.timeoutObj = setTimeout(function() {
        // console.log('正在发送心跳',ws)
        ws.send(token);
        self.serverTimeoutObj = setTimeout(function() {
          ws.close(); //如果onclose会执行reconnect，我们执行ws.close()就行了.如果直接执行reconnect 会触发onclose导致重连两次
        }, self.timeout);
      }, this.timeout);
    },
  };