(ns card-sim.views.card
  "Card and card-list views."
  (:require [re-frame.core :as re-frame]))

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

