(ns card-sim.db
  (:require [card-sim.game :as game]))

(def default-db
  {:deck game/default-deck
   :simulation {:bins {}
                :last-round []}})

