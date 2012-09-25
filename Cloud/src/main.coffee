Parse.Cloud.define 'projectDuration', (request, response) ->
    query = new Parse.Query("Task")
    query.equalTo 'project', request.params.project
    query.find
        success: (results) ->
            response.success [task.get('duration') for task in results].reduce (t, s) -> t + s
        error: (error) ->
            response.error("Task lookup failed")
            
Parse.Cloud.beforeSave 'WorkUnit', (request, response) ->
    task = request.object.get 'task'
    task.increment 'duration', request.object.get 'duration'
    task.save
        success: () ->
            response.success()
        error: (error) ->
            response.error error