(ns card-sim.subs
  (:require [re-frame.core :as re-frame]
            [card-sim.stats :as stats]))

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

(defn build-heatmap-trace
  [data stats-func]
  {:type "heatmap"
   :x (map first (keys data))
   :y (map second (keys data))
   :z (map stats-func (vals data))})

(def default-stats-func stats/freq-mean)
(def heatmap-stats-func {:mean stats/freq-mean
                         :median stats/freq-median})

(re-frame/reg-sub
  ::simulation-graph
  (fn [db]
    {:data [(build-histogram-trace (get-in db [:simulation :bins [0 0]]))]
     :layout {}}))

(re-frame/reg-sub
  ::simulation-heatmap
  (fn [db [_ stats-func]]
    (let [data (get-in db [:simulation :bins])
          stats-func (get heatmap-stats-func stats-func default-stats-func)]
      ;; This is a Plotly heatmap map of good cards (x) and bad cards (y),
      ;; with average round length (z) as the mapped dimension.
      {:layout {}
       :data [(build-heatmap-trace data stats-func)]})))

