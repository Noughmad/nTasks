Parse.Cloud.define 'projectDuration', (request, response) ->
    query = new Parse.Query("Task")
    query.equalTo 'project', request.params.project
    query.find
        success: (results) ->
            response.success [task.get('duration') for task in results].reduce (t, s) -> t + s
        error: (error) ->
            response.error("Task lookup failed")