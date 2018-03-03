(ns card-sim.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [card-sim.subs :as subs]
            [card-sim.events :as events]
            ))

;;; Plotly component
;; Plotly is provided from the cdn so the bundle isn't huge

(defn plotly-render [] [:div.plot])

(defn plotly-did-mount
  [listener this]
  (let [plot-el (reagent/dom-node this)
        {:keys [data layout]} (reagent/props this)
        resize-listener #(js/Plotly.Plots.resize plot-el)]
    (js/Plotly.newPlot plot-el (clj->js data) (clj->js layout))
    ;; Add the resize listener.
    (reset! listener resize-listener)
    (.addEventListener js/window "resize" resize-listener)))

(defn plotly-will-unmount
  [listener this]
  ;; Kill the resize listener.
  (when-not (nil? @listener)
    (.removeEventListener js/window "resize" @listener)
    (reset! listener nil)))

(defn plotly-did-update
  [this]
  (let [plot-el (reagent/dom-node this)
        {:keys [data layout]} (reagent/props this)]
    ;; A little hacky, but plotly usually works if we edit the data directly
    (set! (.-data plot-el) (clj->js data))
    (set! (.-layout plot-el) (clj->js layout))
    ;; Sometimes redraw breaks, in which case we just need a new plot.
    (try
      (js/Plotly.redraw plot-el)
      (catch :default e
        (js/Plotly.newPlot plot-el (clj->js data) (clj->js layout))))))

(defn plotly
  []
  ;; Keep a reference to the window resize listener so we can remove it
  ;; when the component unmounts
  (let [listener (atom nil)]
    (reagent/create-class
      {:reagent-render plotly-render
       :component-did-mount (partial plotly-did-mount listener)
       :component-did-update plotly-did-update
       :component-will-unmount (partial plotly-will-unmount listener)})))

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
     [static-card-list @deck]))

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

