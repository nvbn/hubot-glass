try
  hubot = require 'hubot'
catch
  prequire = require 'parent-require'
  hubot = prequire 'hubot'

uuid = require 'node-uuid'

class Polling extends hubot.Adapter
  constructor: (robot) ->
    super
    @responses = {}

  send: ({user}, strings...) ->
    messages = @responses[user.id] || []
    @responses[user.id] = messages.concat(strings)

  reply: (envelope, strings...) ->
    @send(envelope, strings...)

  run: () ->
    @robot.router.post '/polling/subscribe/', (req, res) =>
      ### Subscribes new user and returns credentials. ###
      user = uuid.v1()
      @responses[user] = []
      res.json user: user

    @robot.router.post '/polling/message/', (req, res) =>
      ### Receives message from user. ###
      {user, text} = req.body
      @robot.receive new hubot.TextMessage(
        @robot.brain.userForId(user), "#{@robot.name} #{text}")
      res.json {ok: true}

    @robot.router.get '/polling/response/:user/', (req, res) =>
      ### Returns new responses to user. ###
      user = req.params.user
      res.json 'responses': @responses[user] || []
      @responses[user] = []

    @emit "connected"

exports.use = (robot) => new Polling(robot)
