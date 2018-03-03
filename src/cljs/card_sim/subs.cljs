(ns card-sim.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::deck
 (fn [db] (:deck db)))

(re-frame/reg-sub
  ::last-round
  (fn [db] (get-in db [:simulation :last-round])))

(defn build-histogram-trace
  [data]
  {:type "bar"
   :x (keys data)
   :y (vals data)})

(re-frame/reg-sub
  ::simulation-graph
  (fn [db]
    {:data [(build-histogram-trace (get-in db [:simulation :bins [0 0]]))]
     :layout {}}))
