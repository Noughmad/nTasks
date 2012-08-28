jQuery ->

    MILI_PER_SECOND = 1000
    MILI_PER_MINUTE = MILI_PER_SECOND * 60
    MILI_PER_HOUR = MILI_PER_MINUTE * 60

    window.TaskStatus =
        TODO: 0
        INPROGRESS: 1
        DONE: 2

    window.formatDuration = (miliseconds) ->
        hours = Math.floor(miliseconds / MILI_PER_HOUR)
        miliseconds -= hours * MILI_PER_HOUR

        minutes = Math.floor(miliseconds / MILI_PER_MINUTE)
        miliseconds -= minutes * MILI_PER_MINUTE

        seconds = Math.floor(miliseconds / MILI_PER_SECOND)

        if hours > 0
            ret = hours + ' h'
            if minutes > 0
                ret += ' '
            if minutes < 10
                ret += '0'
            ret += minutes + ' min'
            return ret

        else
            ret = minutes + ' min ' + seconds + ' s'
            return ret

    class DurationManager
        updateTask: (task) ->
            @clearTask()
            if task
                @task = task
                @interval = setInterval @updateDuration, 1000

        clearTask: () ->
            if @interval
                clearInterval @interval
            @task = null

        updateDuration: ->
            end = new Date
            currentDuration = end - @task.get 'lastStart'
            $('.task-current-duration').html formatDuration currentDuration
            $('.task-' + @task.id + '-total-duration').html formatDuration currentDuration + @task.get 'duration'

    TAB_LOGIN = 0
    TAB_TASKS = 1
    TAB_STATS = 2
    TAB_REPORT = 3

    class State extends Parse.Object
        className: "JSAppState"
        setTab: (tab) ->
            if Parse.User.current()
                @set 'tab', tab
            else
                @set 'tab', TAB_LOGIN
