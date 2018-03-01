# card-sim

An application for simulating and analyzing simple card games (and a project
for learning [re-frame](https://github.com/Day8/re-frame) and brushing up on
[ClojureScript](https://clojurescript.org/)).

This came out of an evening playing [Incan Gold](https://boardgamegeek.com/boardgame/15512/diamant)
and wondering about the average length of a round.  This app simulates many
rounds of the game and displays the frequency of round lengths as a histogram.

## Development Mode

### Run application:

```
lein dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build


To compile clojurescript to javascript:

```
lein build
```

