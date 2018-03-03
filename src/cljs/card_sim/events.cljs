(ns card-sim.events
  (:require [re-frame.core :as re-frame]
            [card-sim.db :as db]
            [card-sim.game :as game]
            ))

(re-frame/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))


(defn update-simulation-map
  "Update the db with the result of a single simulation."
  [db cards]
  (let [card-count (count cards)]
    (-> db
        ;; Set the most recent round.
        (assoc-in [:simulation :last-round] cards)
        ;; Update statistics.
        (update-in [:simulation :bins card-count] (fnil inc 0)))))

;; re-render the dom after this many iterations
(def dom-refresh-interval 1000)

(defn run-simulation
  "Run the simulation a number of times, returning the modified db."
  ;; Run a single simulation.
  ([db deck]
   (update-simulation-map db (game/deal-round deck)))
  ;; Run multiple simulations.
  ([db deck i]
   (cond
     ;; last iteration
     (<= i 0)
     db
     ;; re-render the dom
     (= 0 (mod i dom-refresh-interval))
     (do
       ;; (1) dispatch another event with the remaining iterations
       (re-frame/dispatch ^:flush-dom [::run-simulation (dec i)])
       ;; (2) return the new db (thus ending this event handler)
       (run-simulation db deck))
     ;; next iteration
     :else
     (recur (run-simulation db deck) deck (dec i)))))

(re-frame/reg-event-db
  ::run-simulation
  (fn [db [_ times]]
    (run-simulation db (:deck db) times)))

(re-frame/reg-event-db
  ::reset-simulation
  (fn [db _]
    (update db :simulation merge db/empty-simulation)))

