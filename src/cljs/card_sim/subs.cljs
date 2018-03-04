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

;;; Plotly plots

(defn labels-for
  "Transform data in a trace for textual labels.

  Example:

    (labels-for trace #(.toFixed % 2) [:y])"
  [trace func data-keys]
  (let [data ((apply juxt data-keys) trace)]
    (apply map func data)))

(defn with-labels
  "Add labels to a Plotly trace."
  [trace func & data-keys]
  (let [labels {:text (labels-for trace func data-keys)
                :textposition "auto"
                :insidetextfont {:color "white"}}]
    (merge labels trace)))

(defn annotations-for
  "Add annotations to a Plotly trace."
  [trace func & data-keys]
  ;; Force x and y coords so that the annotation can be placed in the correct
  ;; location.
  (letfn [(transform [x y & args] {:text (apply func args)
                                   :x x
                                   :y y})]
    (labels-for trace transform (concat [:x :y] data-keys))))

(defn heatmap-annotations
  "Build annotations for a heatmap."
  [trace]
  (let [default-annotation {:showarrow false
                            :font {:color "white"}}]
    (as-> trace t
      (annotations-for t #(.toPrecision % 2) :z)
      (map #(merge default-annotation %) t))))

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
          trace (build-histogram-trace data)
          n (reduce + (vals data))
          pct #(-> % (/ n) (* 100) (.toPrecision 3) (str "%"))
          title (str "Round length after "
                     (util/pluralize (first bins-key) " gem")
                     " and " (util/pluralize (second bins-key) " hazard")
                     "<br><i>n = " n "<i>")]
      {:data [(with-labels trace pct :y)]
       :layout {:title title}})))

(re-frame/reg-sub
  ::simulation-heatmap
  (fn [db [_ stats-func]]
    (let [data (get-in db [:simulation :bins])
          stats-kw (or stats-func default-stats-func)
          stats-func (get heatmap-stats-func stats-kw)
          trace (build-heatmap-trace data stats-func)
          title (str (util/ucfirst (name stats-kw))
                     " round length after x gems and y hazards"
                     "<br><i>click to view detail in the histogram</i>")]
      ;; This is a Plotly heatmap map of good cards (x) and bad cards (y),
      ;; with average round length (z) as the mapped dimension.
      {:data [trace]
       :layout {:title title
                :xaxis {:title "Gems"}
                :yaxis {:title "Hazards"}
                :annotations (heatmap-annotations trace)}})))

