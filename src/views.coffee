

class ConfirmDeleteDialog extends Parse.View
    tagName: 'div'

    initialize: (options) ->
        @model = options.model

    hide: ->
        @$el.modal('hide')
        false

    remove: ->
        @model.destroy()
        @hide()

    render: ->
        dialog = $("""
            <div class="modal hide" id="confirm-delete-dialog">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h3>Really delete #{@model.get 'title'}?</h3>
                </div>
                <div class="modal-body">
                    <p>This cannot be undone</p>
                </div>
                <div class="modal-footer">
                    <a class="btn cancel" data-dismiss="modal">Cancel</a>
                    <a class="btn btn-danger delete">Delete</a>
                </div>
            </div>
        """)
        @el = dialog.modal 'keyboard': true, 'backdrop': true
        @$el = $(@el)
        @delegateEvents()
        @el.modal('show': true)
        @

    events:
        'click .delete' : 'remove'
        'click .cancel' : 'hide'

class AddProjectDialog extends Parse.View
    tagName: 'div'
    template = _.template $("#project-dialog-template").html()

    hide: ->
        @el.modal('hide')
        false

    save: =>
        project = new Project
        project.setACL new Parse.ACL Parse.User.current()
        project.set
            user: Parse.User.current()
            title: @$("#project-title").val()
            client: @$("#project-client").val()
        project.save null,
            success: (project) =>
                @hide()
                @collection.fetch()
            error: (project, error) =>
                # TODO: Show error

    render: ->
        dialog = $ template({})
        @el = dialog.modal
            keyboard: true
            backdrop: true
        @$el = $(@el)
        @delegateEvents()
        @el.modal('show' : true)

    events:
        'submit form': 'save'
        'click .cancel': 'hide'

class TaskItemView extends Parse.View
    tagName: 'tr'
    template: _.template($('#task-item-template').html())

    initialize: ->
        _.bindAll @
        @model.bind 'remove', @unrender
        @model.bind 'change:done', @updateCheckBox
        @model.bind 'change:name', @render
        @model.bind 'change:active', @updateTrackingButtons

    render: ->
        @$el.html @template @model.toJSON()
        @hideEdits()
        @updateCheckBox()
        @updateTrackingButtons()
        @

    updateCheckBox: ->
        if @model.get 'done'
            @$el.addClass 'task-done'
            @$('.status').html 'Done'
            @$('.status').addClass 'label-success'
            @$('.status').removeClass 'label-warning'
        else
            @$el.removeClass 'task-done'
            @$('.status').html 'To-Do'
            @$('.status').removeClass 'label-success'
            @$('.status').addClass 'label-warning'

    updateTrackingButtons: ->
        if @model.get 'active'
            @$('.start-tracking').hide()
            @$('.stop-tracking').show()
            @$el.addClass 'info'
        else
            @$('.start-tracking').show()
            @$('.stop-tracking').hide()
            @$el.removeClass 'info'

    unrender: ->
        @$el.remove()

    remove: ->
        @model.destroy()

    setEditable: (editable) ->
        @editable = editable
        if @editable
            @$('.task-editable').show()
            @$('.task-normal').hide()
            @$('.start-tracking').removeClass('btn-mini')
            @$('.start-tracking').addClass('btn-primary')
        else
            @$('.task-editable').hide()
            @$('.task-normal').show()
            @$('.start-tracking').addClass('btn-mini')
            @$('.start-tracking').removeClass('btn-primary')

    start: ->
        @model.startTracking()

    stopTracking: ->
        @model.stopTracking()

    showEdits: ->
        @setEditable true

    hideEdits: ->
        @setEditable false

    rename: ->
        name = @$('.task-name-edit').val()
        if not name or name == @model.get 'name'
            return
        @model.set
            name: name
        @model.save()

    toggleDone: ->
        @model.set 'done', @$('.toggle').is(':checked')
        @model.save()

    toggleButtonClicked: ->
        @model.set 'done', !@model.get 'done'
        @model.save()

    events:
        'click .delete' : 'remove'
        'click .start-tracking' : 'start'
        'click .stop-tracking' : 'stopTracking'
        'click .task-name' : 'showEdits'
        'click .cancel' : 'hideEdits'
        'submit .form-rename-task' : 'rename'
        'change .toggle' : 'toggleDone'
        'click .done-toggle' : 'toggleButtonClicked'

class TaskListView extends Parse.View
    initialize: (options) ->
        _.bindAll @
        @project = options.project

        query = new Parse.Query(Task)
        query.equalTo "project", @project
        @tasks = query.collection()
        @tasks.bind 'add', @appendTask
        @tasks.bind 'reset', @resetTasks
        @tasks.fetch()
        @showCompleted()
        @render()

    render: ->
        console.log 'Rendering task list for ' + @project.get 'title'
        @$el.html """
            <div class="page-header">
                <h1>#{@project.get 'title'}</h1>
            </div>
            <table class="table table-hover">
                <thead>
                    <tr>
                        <td>Status</td>
                        <td>Task Name</td>
                        <td>Time Spent</td>
                        <td></td>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <form class="form form-inline form-new-task">
                <input type="text" id="new-task-name" placeholder="New Task Name">
                <button type="submit" class="btn">Add Task</button>
            </form>
        """
        @showCompleted()
        @

    showCompleted: ->
        @completedShown = true
        @$('.toggle-show-done').html 'Hide completed'
        @$('.task-done').show('fast')

    hideCompleted: ->
        @completedShown = false
        @$('.toggle-show-done').html 'Show all'
        @$('.task-done').hide('fast')

    toggleCompleted: ->
        if @completedShown
            @hideCompleted()
        else
            @showCompleted()

    appendTask: (task) =>
        task_view = new TaskItemView model: task
        @$('tbody').append task_view.render().el

    resetTasks: =>
        @$('tbody').html("");
        @tasks.each(@appendTask);

    addTask: ->
        name = @$('#new-task-name').val()
        if not name
            return
        task = new Task
        task.setACL new Parse.ACL Parse.User.current()
        task.set
            name: name
            project: @project
            user: Parse.User.current()
        console.log "Adding task " + name + " to project " + @project.get 'title'
        task.save null,
            success: (task) =>
                @tasks.fetch()
            error: (task, error) =>
        @$('#new-task-name').val("")

    events:
        'submit .form-new-task' : 'addTask'
        'click .toggle-show-done' : 'toggleCompleted'

class ProjectListView extends Parse.View
    initialize: ->
        _.bindAll @

        projects.bind 'add', @appendProject
        projects.bind 'reset', @resetProjects
        projects.fetch()
        @render()

    render: ->
        @$el.html """
            <ul class="nav nav-pills nav-stacked"></ul>
            <a class="btn" id="add-project-button">New Project</a>
        """
        @

    appendProject: (project) ->
        project_view = new ProjectItemView model: project
        @$('ul').append project_view.render().el

    resetProjects: ->
        @$('ul').html("")
        projects.each(@appendProject)

    addProject: ->
        dialog = new AddProjectDialog collection: projects
        dialog.render()

    events: 'click #add-project-button' : 'addProject'

class ProjectItemView extends Parse.View
    tagName: 'li'
    template: _.template($('#project-item-template').html())

    initialize: ->
        _.bindAll @
        @model.bind 'change', @render
        @model.bind 'remove', @unrender
        @model.bind 'change:selected', @selectedStateChanged

    render: =>
        context = @model.toJSON()
        if not @model.has 'selected'
            context['selected'] = false
        @$el.html @template(context)
        @

    unrender: =>
        @$el.remove()

    remove: ->
        dialog = new ConfirmDeleteDialog(model: @model)
        dialog.render()

    select: ->
        @model.select()
        false

    selectedStateChanged: ->
        if @model.get 'selected'
            @$el.addClass 'active'
        else
            @$el.removeClass 'active'

    events:
        'click .delete' : 'remove'
        'click .select' : 'select'

class NavigationBar extends Parse.View
    el: $ '#navigation'
    template: _.template $('#navigation-bar-template').html()

    initialize: ->
        @task = null
        @render

    render: ->
        context =
            user: Parse.User.current()
            location: Backbone.history.fragment
        @$el.html @template(context)
        @showTask @task
        @

    events:
        'click .log-out': 'logOut'
        'click .stop-tracking': 'stopTracking'

    logOut: ->
        Parse.User.logOut()
        vent.trigger "user:currentChanged"

    showTask: (task) ->
        @task = task
        if task
            @$('.active-task-display .task-name').html task.get 'name'
            @$('.active-task-display').show()
        else
            @$('.active-task-display').hide()

    stopTracking: ->
        if @task
            @task.stopTracking()


class ManageTasksView extends Parse.View
    el: $ '#main-container'
    template: _.template $('#main-view-template').html()

    initialize: ->
        _.bindAll @
        @render()

    render: ->
        @$el.html @template({})
        new ProjectListView {el: $ '#project-list'}
        @

class LogInView extends Parse.View
    el: $ '#main-container'
    template: _.template $('#login-form-template').html()

    events:
        'submit form.login-form': 'logIn'
        'submit form.signup-form': 'signUp'

    initialize: ->
        _.bindAll @
        @render()

    render: ->
        @$el.html @template({})

    logIn: ->
        username = $('#login-username').val()
        password = $('#login-password').val()

        Parse.User.logIn username, password,
            success: (user) ->
                vent.trigger "user:currentChanged"
            error: (user, error) =>
                @$(".login-form .error").html("Invalid username or password. Please try again.").show()
                @$(".login-form button").removeAttr("disabled")

        @$(".login-form button").attr("disabled", "disabled")
        false

    signUp: ->
        username = $("#signup-username").val()
        password = $("#signup-password").val()

        Parse.User.signUp username, password, { ACL: new Parse.ACL() },
            success: (user) ->
                vent.trigger "user:currentChanged"
            error: (user, error) =>
                @$(".signup-form .error").html(error.message).show()
                @$(".signup-form button").removeAttr("disabled")

        @$(".signup-form button").attr("disabled", "disabled")
        return false;

class StatsView extends Parse.View
    el: $ '#main-container'
    template: _.template $('#stats-view-template').html()

    initialize: ->
        _.bindAll @
        $.jqplot.config.enablePlugins = true

        console.log 'Init StatsView'
        query = new Parse.Query(Task)
        query.equalTo 'user', Parse.User.current()
        @tasks = query.collection()
        @tasks.fetch
            success: (tasks) =>
                console.log 'Success getting tasks'
                @setTasks(tasks)
            error: (error) ->
                console.log error.message
        @render()

    render: ->
        @$el.html @template({})

    setTasks: (tasks) ->
        data = {}
        tasks.each (task) ->
            p = task.get 'project'
            if not data.hasOwnProperty(p.id)
                data[p.id] = 0
            data[p.id] = data[p.id] + task.get 'duration'

        values = []
        labels = []
        for key, value of data
            p = projects.get(key)
            labels.push if p then p.get 'title' else key
            values.push Math.round(value / MILI_PER_HOUR * 10) / 10

        $.jqplot 'plot-placeholder', [values],
            animate: true
            seriesDefaults:
                renderer: $.jqplot.BarRenderer
                pointLabels: {show: true}
            axes:
                xaxis:
                    renderer: $.jqplot.CategoryAxisRenderer
                    ticks: labels
            highlighter: {show: false}

class AppView extends Parse.View
    showProject: (project) ->
        if @task_list
            delete @task_list
        @task_list = new TaskListView
            el: $ "#task-list"
            project: project

    initialize: ->
        _.bindAll @
        @bind "user:currentChanged", =>
            @render()
        @view = null
        @task_list = null
        @render()

    render: ->
        @showTasks()
        @

    showTasks: ->
        if @view
            @view.undelegateEvents()
            delete @view

        if Parse.User.current()
            @view = new ManageTasksView
        else
            @view = new LogInView

    showStats: ->
        if @view
            @view.undelegateEvents()
            delete @view

        if Parse.User.current()
            @view = new StatsView
        else
            @view = new LogInView
