(ns card-sim.db
  (:require [card-sim.game :as game]))

(def empty-simulation
  {:bins {}
   :last-round []
   :is-running false})

(defn simulation-running?  [db] (get-in db [:simulation :is-running]))

(def default-db
  {:deck game/default-deck
   :simulation empty-simulation})

