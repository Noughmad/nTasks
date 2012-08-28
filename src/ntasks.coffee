    state = new State

    durationManager = new DurationManager
    state.bind 'change:activeTask', (task) ->
        if task
            durationManager.updateTask task
        else
            durationManager.clearTask

    projects = []

    updateProjects = () ->
        query = new Parse.Query(Project)
        query.equalTo 'user', Parse.User.current()
        projects = new ProjectList null, {query: query}

    state.bind 'change:user', (user) ->
        if Parse.User.current()
            updateProjects()
            projects.fetch()
        else
            projects = []

    class AppRouter extends Parse.Router
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

    state.bind "change:selectedProject", (state, project) ->
        document.title = project.get('title') + ' - nTasks'
        app.showProject(project)
        router.navigate "/project/" + project.id

    state.bind "change:user", ->
        nav.render()
        app.render()

    state.bind "change:activeTask", (task) ->
        nav.showTask task

    Parse.history.start()

