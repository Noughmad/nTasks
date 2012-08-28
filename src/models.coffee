    class Task extends Parse.Object
        className: "Task"

        defaults:
            active: false
            units: []
            duration: 0
            status: TaskStatus.TODO

        startTracking: ->
            if @get 'active'
                return

            @set
                active: true
                lastStart: new Date
            @save()
            state.set 'activeTask', @

        stopTracking: ->
            if not @get 'active'
                return

            start = @get 'lastStart'
            end = new Date
            duration = end - start

            @set
                active: false
                lastStart: null
            @increment 'duration', duration
            @add 'units',
                start: start
                end: end
                duration: duration
            @save()

            state.set 'activeTask', null

    class Project extends Parse.Object
        className: "Project"
        select: ->
            state.set 'selectedProject', @

    class TaskList extends Parse.Collection
        model: Task

    class ProjectList extends Parse.Collection
        model: Project