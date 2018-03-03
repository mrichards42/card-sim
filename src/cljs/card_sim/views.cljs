(ns card-sim.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [clojure.string :as str]
            [card-sim.subs :as subs]
            [card-sim.events :as events]
            [card-sim.views.plotly :as plotly]
            [card-sim.views.card :as card]
            ))

;;; Simulation components

(defn simulation-histogram
  "A histogram of round-length frequency."
  ([bins-key]
   (simulation-histogram bins-key {}))
  ([bins-key events]
   ;; Dynamic subscription to simulation-graph
   (let [graph-data (re-frame/subscribe [::subs/simulation-graph] [bins-key])]
     (fn [] [plotly/plot (into @graph-data events)]))))

(defn simulation-heatmap
  "A heatmap of average round-length for any point in the round."
  ([stats-func]
   (simulation-heatmap stats-func {}))
  ([stats-func events]
   (let [graph-data (re-frame/subscribe [::subs/simulation-heatmap stats-func])]
     (fn [] [plotly/plot (into @graph-data events)]))))

(defn simulation-graphs
  "Combination of heatmap and detail histogram."
  []
  (let [detail-key (reagent/atom [0 0])
        ;; Set detail-key to the coords from the clicked heatmap value.
        click-handler (fn [data]
                        (let [point (-> data .-points (aget 0))]
                          (reset! detail-key [(.-x point) (.-y point)])))]
    (fn []
      [:div.graph-container
       [simulation-heatmap :mean {:on-plotly-click click-handler}]
       ;; Note this is *not* derefed since we're using this ratom as a dynamic
       ;; subscription in the simulation-histogram component.
       [simulation-histogram detail-key]])))

(defn simulation-contols
  []
  (let [simulation-count (reagent/atom 1)]
    (fn []
      [:div
       ; run simulation button
       [:button
        {:on-click (fn []
                     (re-frame/dispatch [::events/run-simulation
                                             @simulation-count]))}
        "Run Simulation(s)"]
       ; simulation count input
       [:input {:type "number"
                :value @simulation-count
                :on-change #(reset! simulation-count (-> % .-target .-value))}]
       ; reset simulation button
       [:button
        {:on-click #(re-frame/dispatch [::events/reset-simulation])}
        "Reset"]])))

;;; Main page

(defn main-panel []
  [:div.app
   [:div "All cards" [card/card-list [::subs/deck]]]
   [simulation-contols]
   [:div "Last round" [card/card-list [::subs/last-round]]]
   [simulation-graphs]])

