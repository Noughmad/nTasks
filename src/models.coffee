 class Task extends Parse.Object
    className: "Task"

    defaults:
        done: false
        active: false
        units: []
        duration: 0

    startTracking: ->
        if @get 'active'
            return

        vent.trigger 'task:started', @
        @set
            active: true
            lastStart: new Date
        @save null
        @bind 'remove', -> vent.trigger 'task:stopped', @

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

        @save null
        vent.trigger 'task:stopped', @
        @unbind 'remove', null, @

class Project extends Parse.Object
    className: "Project"
    select: ->
        @collection.select @

class TaskList extends Parse.Collection
    model: Task

class ProjectList extends Parse.Collection
    model: Project

    selectProject: (project) ->
        @selectedProject?.set 'selected', false
        project.set 'selected', true
        @selectedProject = @
        vent.trigger 'project:selected', @