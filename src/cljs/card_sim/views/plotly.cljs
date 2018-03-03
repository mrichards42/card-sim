(ns card-sim.views.plotly
  "Plotly.js component"
  (:require [reagent.core :as reagent]
            [clojure.string :as str]))

(defn plotly-events
  "Return a sequence of [event-name handler] pairs of Plotly events."
  [props]
  (letfn [(is-event? [kw] (str/starts-with? (name kw) "on-plotly-"))
          (event-name [kw] (-> (name kw)
                               (str/replace #"^on-" "")
                               (str/replace #"-" "_")))]
    (->> props
      (filter #(is-event? (first %)))
      (map #(vector (event-name (first %)) (second %))))))

(defn rebind-events
  "Remove all events from the plotly element and bind new events."
  [plot-el props]
  (.removeAllListeners plot-el)
  (doseq [[event-name handler] (plotly-events props)]
    (.on plot-el event-name handler)))

(defn new-plot
  "Create a new Plotly plot from props."
  [plot-el {:keys [data layout] :as props}]
  (js/Plotly.newPlot plot-el (clj->js data) (clj->js layout))
  (rebind-events plot-el props))

(defn update-plot
  "Update an existing plot."
  [plot-el {:keys [data layout] :as props}]
  ;; A little hacky, but plotly usually works if we edit the data directly
  (set! (.-data plot-el) (clj->js data))
  (set! (.-layout plot-el) (clj->js layout))
  ;; Sometimes redraw breaks, in which case we just need a new plot.
  (try
    (js/Plotly.redraw plot-el)
    (catch :default e
      (new-plot plot-el props))))


;;; Lifecycle handlers

(defn plot-render [] [:div.plotly-plot])

(defn plot-did-mount
  [listener this]
  (let [plot-el (reagent/dom-node this)
        props (reagent/props this)
        resize-listener #(js/Plotly.Plots.resize plot-el)]
    (new-plot plot-el props)
    ;; Add the resize listener.
    (reset! listener resize-listener)
    (.addEventListener js/window "resize" resize-listener)))

(defn plot-will-unmount
  [listener this]
  ;; Remove all Plotly data (including Plotly events)
  (js/Plotly.purge (reagent/dom-node this))
  ;; Kill the resize listener.
  (when-not (nil? @listener)
    (.removeEventListener js/window "resize" @listener)
    (reset! listener nil)))

(defn plot-did-update
  [this]
  (let [plot-el (reagent/dom-node this)
        props (reagent/props this)]
    (update-plot plot-el props)))

(defn plot
  "The plot component.

  Include in reagent or re-frame like so:

    [plotly/plot {:data {}
                  :layout {}
                  :on-plotly-click (fn [])}]

  Typically graph data should come from a re-frame subscription:

    (let [graph-data (re-frame/subscribe [::subs/my-graph])]
      [plotly/plot (into @graph-data {:on-plotly-click (fn [])})])"
  []
  ;; Keep a reference to the window resize listener so we can remove it when
  ;; the component unmounts.
  (let [listener (atom nil)]
    (reagent/create-class
      {:reagent-render plot-render
       :component-did-mount (partial plot-did-mount listener)
       :component-did-update plot-did-update
       :component-will-unmount (partial plot-will-unmount listener)})))

