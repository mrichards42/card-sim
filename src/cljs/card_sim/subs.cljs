(ns card-sim.subs
  (:require [re-frame.core :as re-frame]
            [card-sim.stats :as stats]
            [card-sim.util :as util]))

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

(def default-stats-func :mean)
(def heatmap-stats-func {:mean stats/freq-mean
                         :median stats/freq-median})

(re-frame/reg-sub
  ::simulation-graph
  ;; This has to support dynamic subscriptions *and* regular subscriptions
  (fn [db [_ bins-key] [dynamic-bins-key]]
    (let [bins-key (or bins-key dynamic-bins-key [0 0])
          data (get-in db [:simulation :bins bins-key])
          title (str "Round length after "
                     (util/pluralize (first bins-key) " gem")
                     " and " (util/pluralize (second bins-key) " hazard")
                     "<br><i>n = " n "<i>")]
      {:data [(build-histogram-trace data)]
       :layout {:title title}})))

(re-frame/reg-sub
  ::simulation-heatmap
  (fn [db [_ stats-func]]
    (let [data (get-in db [:simulation :bins])
          stats-kw (or stats-func default-stats-func)
          stats-func (get heatmap-stats-func stats-kw)
          title (str (util/ucfirst (name stats-kw))
                     " round length after x gems and y hazards"
                     "<br><i>click to view detail in the histogram</i>")]
      ;; This is a Plotly heatmap map of good cards (x) and bad cards (y),
      ;; with average round length (z) as the mapped dimension.
      {:data [(build-heatmap-trace data stats-func)]
       :layout {:title title
                :xaxis {:title "Gems"}
                :yaxis {:title "Hazards"}}})))
