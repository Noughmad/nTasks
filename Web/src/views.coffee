    class ConfirmDeleteDialog extends Parse.View
        tagName: 'div'
        template: _.template $('#delete-dialog-template').html()

        initialize: (options) ->
            @model = options.model

        hide: ->
            @$el.modal('hide')
            false

        remove: ->
            @model.destroy()
            @hide()

        render: ->
            dialog = $ @template @model.toJSON()
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
            @model.bind 'change:active', @updateActive
            state.bind 'change:selectedTask', @render
            @doShowActions = false
            @render()
            @updateActive()

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
            false

        stopTracking: ->
            @model.stopTracking()
            false

        updateActive: ->
            if @model.get 'active'
                @$el.addClass 'info'
            else
                @$el.removeClass 'info'

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

        taskUrgent: ->
            @setTaskStatus TaskStatus.URGENT

        taskTodo: ->
            @setTaskStatus TaskStatus.TODO

        taskProgress: ->
            @setTaskStatus TaskStatus.INPROGRESS

        taskDone: ->
            @setTaskStatus TaskStatus.DONE

        setTaskStatus: (status) ->
            @model.set 'status', status
            @model.save()
            false

        showActions: ->
            if not @model.get 'active'
                @$('.start-tracking').addClass 'btn-primary'
            false

        hideActions: ->
            @$('.start-tracking').removeClass 'btn-primary'
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
            'click .status-urgent' : 'taskUrgent'
            'click .status-todo' : 'taskTodo'
            'click .status-progress' : 'taskProgress'
            'click .status-done' : 'taskDone'

    class TaskListView extends Parse.View
        template: _.template $('#task-list-template').html()
    
        initialize: (options) ->
            @project = options.project
            _.bindAll @

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
            @$el.html @template @project.toJSON()
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

        addTask: =>
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
            @tasks.add task
            task.save()
            @$('#new-task-name').val("")

        sort: (field, descending) ->
            header = @$('.sort-' + field)
            icon = @$('.sort-' + field + ' i')

            header_asc = 'sort-' + field + '-asc'
            header_desc = 'sort-' + field + '-desc'
            icon_asc = 'icon-chevron-down'
            icon_desc = 'icon-chevron-up'

            if descending
                @tasks.comparator = (one, two) ->
                    if one.get(field) > two.get(field)
                        return -1
                    else if one.get(field) < two.get(field)
                        return 1
                    else
                        return 0
                header.removeClass header_desc
                header.addClass header_asc
                icon.removeClass icon_desc
                icon.addClass icon_asc
            else
                @tasks.comparator = (task) ->
                    task.get field
                header.removeClass header_asc
                header.addClass header_desc
                icon.removeClass icon_asc
                icon.addClass icon_desc
            @tasks.sort()

        sortNameAscending: ->
            @sort 'name', false

        sortNameDescending: ->
            @sort 'name', true

        sortStatusAscending: ->
            @sort 'status', false

        sortStatusDescending: ->
            @sort 'status', true

        sortDurationAscending: ->
            @sort 'duration', false

        sortDurationDescending: ->
            @sort 'duration', true

        events:
            'submit .form-new-task' : 'addTask'
            'click .toggle-show-done' : 'toggleCompleted'
            'click .sort-name-asc' : 'sortNameAscending'
            'click .sort-name-desc' : 'sortNameDescending'
            'click .sort-status-asc' : 'sortStatusAscending'
            'click .sort-status-desc' : 'sortStatusDescending'
            'click .sort-duration-asc' : 'sortDurationAscending'
            'click .sort-duration-desc' : 'sortDurationDescending'
            
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
            false

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
            false


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
            'click button.login-facebook' : 'logInWithFacebook'

        logInWithFacebook: ->
            console.log "Trying to log in with FB"
            Parse.FacebookUtils.logIn null,
                success: (user) ->
                    state.set 'user', user
                error: (user, error) ->
                    console.log "User cancelled the Facebook login or did not fully authorize."
            false

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
            Parse.Cloud.run 'projectsWithDurations', {},
                success: (results) =>
                    @showStats results
                error: (error) ->
                    console.log error.message
            @render()

        render: ->
            @$el.html @template({})

        showStats: (results) ->
            console.log results
            values = (Math.round( (if p.duration then p.duration else 0) / MILI_PER_HOUR * 10) / 10 for p in results)
            labels = (p.title for p in results)

            console.log values
            console.log labels

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
                @task_list.undelegateEvents()
                delete @task_list
            @task_list = new TaskListView
                el: $ "#task-list"
                project: project

        initialize: ->
            _.bindAll @
            state.bind 'change:tab', @render
            @view = null
            @task_list = null
            @render()

        render: ->
            switch state.get 'tab'
                when TAB_LOGIN then @view = new LogInView
                when TAB_TASKS then @view = new ManageTasksView
                when TAB_STATS then @view = new StatsView
            @