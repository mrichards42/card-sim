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
  ([]
   (simulation-histogram [0 0]))
  ([bins-key]
   (let [graph-data (re-frame/subscribe [::subs/simulation-graph bins-key])]
     (fn [] [plotly/plot @graph-data])))

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
     [:div "Last round" [card/card-list [::subs/last-round]] ]
     [simulation-histogram]])

