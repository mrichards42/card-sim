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
   ;; Dynamic subscription to simulation-histogram
   (let [data (re-frame/subscribe [::subs/simulation-histogram] [bins-key])]
     (fn [] [plotly/plot (into @data events)]))))

(defn simulation-heatmap
  "A heatmap of average round-length for any point in the round."
  ([stats-func]
   (simulation-heatmap stats-func {}))
  ([stats-func events]
   ;; Dynamic subscription to simulation-heatmap
   (let [data (re-frame/subscribe [::subs/simulation-heatmap] [stats-func])]
     (fn [] [plotly/plot (into @data events)]))))

(defn simulation-plots
  "Combination of heatmap and detail histogram."
  []
  (let [click-key (reagent/atom [0 0])
        hover-key (reagent/atom @click-key)
        heatmap-aggregation (re-frame/subscribe [::subs/heatmap-aggregation])
        ;; Update the detail histogram based on the last clicked heatmap value
        ;; or, if hovering, the value under the cursor
        make-handler (fn [ratom] #(let [point (-> % .-points (aget 0))
                                        new-key [(.-x point) (.-y point)]]
                                    (reset! ratom new-key)))
        on-click (make-handler click-key)
        on-hover (make-handler hover-key)
        on-unhover #(reset! hover-key @click-key)]
    (fn []
      [:div.plot-container
       ;; Note that neither of the ratoms are derefed, since they're both
       ;; used in dynamic subscriptions.
       [simulation-heatmap heatmap-aggregation {:on-plotly-click on-click
                                                :on-plotly-hover on-hover
                                                :on-plotly-unhover on-unhover}]
       [simulation-histogram hover-key]])))

(defn simulation-contols
  []
  (let [simulation-count (reagent/atom 1)
        heatmap-aggregation (re-frame/subscribe [::subs/heatmap-aggregation])]
    (fn []
      [:form.sim-control
       {:action "javascript:void(0)"
        :on-submit #(re-frame/dispatch [::events/run-simulation
                                        @simulation-count])}
       ; run simulation button
       [:button
        {:type "submit"}
        "Run Simulation(s)"]
       ; simulation count input
       [:input {:class "sim-control__count"
                :type "number"
                :value @simulation-count
                :on-change #(reset! simulation-count (-> % .-target .-value))}]
       ; reset simulation button
       [:button
        {:type "button"
         :on-click #(re-frame/dispatch [::events/reset-simulation])}
        "Reset"]
       ; heatmap aggregation drop-down
       [:label
        "Heatmap aggregation"
        [:select
         {:value (name @heatmap-aggregation)
          :on-change #(re-frame/dispatch [::events/set-aggregation
                                          (-> % .-target .-value)]) }
         [:option "mean"]
         [:option "median"]]]])))

;;; Main page

(defn main-panel []
  [:div.app
   [:div "All cards" [card/card-list [::subs/deck]]]
   [simulation-contols]
   [:div "Last round" [card/card-list [::subs/last-round]]]
   [simulation-plots]])

