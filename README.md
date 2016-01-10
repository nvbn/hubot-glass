# Hubot for Google Glass

[![gif with examples](https://raw.githubusercontent.com/nvbn/hubot-glass/master/example.gif)](https://raw.githubusercontent.com/nvbn/hubot-glass/master/example.gif)

## Structure

* `HubotForGlass` android app for Glass written in kotlin;
* `hubot-polling` hubot adapter written in coffeescript.

## Usage

Setup hubot:

```bash
npm install -g yo hubot
mkdir bot
cd bot
yo hubot
npm link ../hubot-polling
./bin/hubot -a polling
```

Setup glass app:

* change `HUBOT_URL` in `HubotForGlass/app/src/main/kotlin/com/nvbn/hubotforglass/RecognizerService.kt`
* build app with gradle or Android Studio.

