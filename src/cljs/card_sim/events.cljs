(ns card-sim.events
  (:require [re-frame.core :as re-frame]
            [card-sim.db :as db]
            [card-sim.game :as game]
            ))

(re-frame/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))

(defn update-simulation-bins
  "Add this round to bins.

  Bins data is a map of [good-count bad-count] pairs to a map of round-length
  frequencies.  For instance, to create a histogram of all round lengths,
  you could use the following let binding:

    (let [frequencies (get bins [0 0])])

  Or to create a heatmap of the average length based on game state:

    (let [good-counts (map first (keys bins))
          bad-counts (map second (keys bins))
          avg-length (map avg (vals bins))])

  Example:

    Given a round of cards: [gem, spider, gem, mummy, spider] the following
    data woud be added to the heatmap dataset:

      [0 0] -> 5 ; initial state: 0 good cards, 0 bad cards, and 5 left
      [1 0] -> 4 ; after 'gem':    1 good, 0 bad, and 4 left
      [1 1] -> 3 ; after 'spider': 1 good, 1 bad, 3 left
      [2 1] -> 2 ; after 'gem':    2 good, 1 bad, 2 left
      [2 2] -> 1 ; after 'mummy':  2 good, 2 bad, 1 left

    The game ends with 'spider', and 0 remaining cards, which is not a useful
    datapoint, so the last card is ignored."
  [bins cards]
  (loop [bins bins
         good-count 0
         bad-count 0
         [first-card & rest-cards :as cards] cards]
    (let [card-count (count cards)]
      (if (= 0 card-count)
        ;; The last card ends the game, so it is not added to the map
        bins
        ;; Add to the frequency map for this [good, bad] pair
        (let [bins-key [[good-count bad-count] card-count]
              bins (update-in bins bins-key (fnil inc 0))]
          ;; Update good-count *or* bad-count and recur
          (if (game/good-card? first-card)
            (recur bins (inc good-count) bad-count rest-cards)
            (recur bins good-count (inc bad-count) rest-cards)))))))

(defn update-simulation-map
  "Update the db with the result of a single simulation."
  [db cards]
  (let [card-count (count cards)]
    (-> db
        ;; Set the most recent round.
        (assoc-in [:simulation :last-round] cards)
        ;; Update statistics.
        (update-in [:simulation :bins] #(update-simulation-bins % cards)))))

;; re-render the dom after this many iterations
(def dom-refresh-interval 1000)

(defn run-simulation
  "Run the simulation a number of times, returning the modified db."
  ;; Run a single simulation.
  ([db deck]
   (update-simulation-map db (game/deal-round deck)))
  ;; Run multiple simulations.
  ([db deck i]
   (cond
     ;; last iteration
     (<= i 0)
     db
     ;; re-render the dom
     (= 0 (mod i dom-refresh-interval))
     (do
       ;; (1) dispatch another event with the remaining iterations
       (re-frame/dispatch ^:flush-dom [::run-simulation (dec i)])
       ;; (2) return the new db (thus ending this event handler)
       (run-simulation db deck))
     ;; next iteration
     :else
     (recur (run-simulation db deck) deck (dec i)))))

(re-frame/reg-event-db
  ::run-simulation
  (fn [db [_ times]]
    (run-simulation db (:deck db) times)))

(re-frame/reg-event-db
  ::reset-simulation
  (fn [db _]
    (update db :simulation merge db/empty-simulation)))

