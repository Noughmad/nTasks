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
            @model.bind 'change', @render
            state.bind 'change:selectedTask', @render
            @doShowActions = false
            @render()

        render: ->
            context = @model.toJSON()
            context['selected'] = ((state.get 'selectedTask') is @)
            context['showActions'] = @doShowActions
            @$el.html @template context
            @

        unrender: ->
            @$el.remove()

        remove: ->
            @model.stopTracking()
            @model.destroy()

        start: ->
            @model.startTracking()

        stopTracking: ->
            @model.stopTracking()

        select: ->
            state.set 'selectedTask', @
            false

        unselect: ->
            if state.get 'selectedTask' is @
                state.set 'selectedTask', null
            false

        saveForm: ->
            name = @$('.task-name-edit').val()
            if name
                if name != @model.get 'name'
                    @model.set 'name', name
                    @model.save()
                state.set 'selectedTask', null

        taskTodo: ->
            @model.set 'status', TaskStatus.TODO
            @model.save()
            false

        taskProgress: ->
            @model.set 'status', TaskStatus.INPROGRESS
            @model.save()
            false

        taskDone: ->
            @model.set 'status', TaskStatus.DONE
            @model.save()
            false

        showActions: ->
            @doShowActions = true
            @render()
            false

        hideActions: ->
            @doShowActions = false
            @render
            false

        events:
            'mouseenter' : 'showActions'
            'mouseleave' : 'hideActions'
            'click .delete' : 'remove'
            'click .start-tracking' : 'start'
            'click .stop-tracking' : 'stopTracking'
            'click .task-name' : 'select'
            'click .cancel' : 'unselect'
            'submit .form-edit-task' : 'saveForm'
            'click .status-todo' : 'taskTodo'
            'click .status-progress' : 'taskProgress'
            'click .status-done' : 'taskDone'

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
                    <div class="input-append">
                        <input type="text" id="new-task-name" placeholder="New Task Name">
                        <button type="submit" class="btn">Add Task</button>
                    </div>
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
            task.save()
            @$('#new-task-name').val("")

        events:
            'submit .form-new-task' : 'addTask'
            'click .toggle-show-done' : 'toggleCompleted'

    class ProjectListView extends Parse.View
        initialize: ->
            _.bindAll @
            updateProjects()

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
            state.bind 'change:selectedProject', @selectedStateChanged

        render: =>
            @$el.html @template @model.toJSON()
            @

        unrender: =>
            @$el.remove()

        remove: ->
            dialog = new ConfirmDeleteDialog(model: @model)
            dialog.render()

        select: ->
            @model.select()
            false

        selectedStateChanged: (state, project) ->
            if project is @model
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
            _.bindAll @
            state.bind 'change:activeTask', @showTask
            @task = null
            @render

        render: ->
            context =
                user: Parse.User.current()
                location: Parse.history.fragment
            @$el.html @template(context)
            @showTask @task
            @

        events:
            'click .log-out': 'logOut'
            'click .stop-tracking': 'stopTracking'

        logOut: ->
            Parse.User.logOut()
            state.set 'user', null

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
                    state.set 'user', user
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
                    state.set 'user', user
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
            state.bind 'change:tab', render
            @view = null
            @task_list = null
            @render()

        render: ->
            t = state.get 'tab'
            if t is TAB_LOGIN
                @view = new LogInView
            else if t is TAB_TASKS
                @view = new ManageTasksView
            else if t is TAB_STATS
                @view = new StatsView
            @