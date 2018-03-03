(ns card-sim.db
  (:require [card-sim.game :as game]))

(def empty-simulation
  {:bins {}
   :last-round []})

(def default-db
  {:deck game/default-deck
   :simulation empty-simulation})

