(ns card-sim.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [cljsjs.plotly]
            [card-sim.subs :as subs]
            [card-sim.events :as events]
            ))

;;; Plotly component

(defn plotly-render [] [:div.plot])

(defn plotly-did-mount
  [this]
  (let [plot-el (reagent/dom-node this)
        {:keys [data layout]} (reagent/props this)]
    (js/Plotly.newPlot plot-el (clj->js data) (clj->js layout))))

(defn plotly-did-update
  [this]
  (let [plot-el (reagent/dom-node this)
        {:keys [data layout]} (reagent/props this)]
    (println "Updating plotly with" (clj->js {:data data :layout layout}))
    ;; A little hacky, but plotly will work fine if we edit the data directly
    (set! (.-data plot-el) (clj->js data))
    (set! (.-layout plot-el) (clj->js layout))
    (println "Data is set")
  (js/Plotly.redraw plot-el)))

(defn plotly
  []
  (reagent/create-class {:reagent-render plotly-render
                         :component-did-mount plotly-did-mount
                         :component-did-update plotly-did-update}))

;;; Cards

(defn card-class [card] (str "card card--" (name card)))

(defn static-card-list
  "Card list with a static list of cards."
  [cards]
  [:ul {:class "card-list"}
   (for [card cards]
     [:li {:class (card-class card)} card])])

(defn card-list
  "Card list component with a subscription."
  [subscribe-args]
   (let [deck (re-frame/subscribe subscribe-args)]
     (static-card-list @deck)))

;;; Simulation components

(defn simulation-graph []
  "A bar graph for a simulation."
  (let [graph-data (re-frame/subscribe [::subs/simulation-graph])]
    (fn [] [plotly @graph-data])))

(defn simulation-contols
  []
  (let [simulation-count (reagent/atom 1)]
    (fn []
      [:div
       ; run simulation button
       [:button
        {:on-click (fn []
                     (println "simulation start" (js/Date.now))
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

(defn main-panel []
  [:div.app
   [:div "All cards" [card-list [::subs/deck]]]
   [simulation-contols]
     [:div "Last round" [card-list [::subs/last-round]] ]
     [simulation-graph]])

