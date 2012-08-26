jQuery ->

    durationManager = new DurationManager
    vent.bind 'task:started', (task) ->
        durationManager.updateTask task

    vent.bind 'task:stopped', () ->
        durationManager.clearTask

    projects = []

    updateProjects = () ->
        query = new Parse.Query(Project)
        query.equalTo 'user', Parse.User.getCurrentUser()
        projects = new ProjectList query: query

    vent.bind 'user:currentChanged', () ->
        if Parse.User.current()
            updateProjects()
            projects.fetch()
        else
            projects = []

    class AppRouter extends Backbone.Router
        routes:
            "project/:id": 'showProject'
            "stats": 'showStats'
            "*actions": 'index'

        index: (actions) ->
            console.log "Showing index " + actions
            app.showTasks()

        showProject: (id) =>
            project = projects.get id
            if project
                project.select()

        showStats: ->
            app.showStats()

    Parse.initialize(PARSE_APPLICATION_ID, PARSE_JAVASCRIPT_KEY)

    router = new AppRouter
    app = new AppView
    nav = new NavigationBar

    router.bind "all", (route, router) ->
        nav.render()

    vent.bind "project:selected", (project) ->
        document.title = project.get('title') + ' - nTasks'
        app.showProject(project)
        router.navigate "/project/" + project.id

    vent.bind "user:currentChanged", ->
        nav.render()
        app.render()

    vent.bind "task:started", (task) ->
        nav.showTask task

    vent.bind "task:stopped", ->
        nav.showTask null

    Backbone.history.start()

