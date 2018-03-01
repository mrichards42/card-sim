(ns card-sim.game)

(def bad-cards #{::snake ::rocks ::mummy ::spider ::fire})

(defn bad-card? [card] (contains? bad-cards card))

(defn good-card? [card]  (not (bad-card? card)))

(defn build-deck
  "Build a deck of cards given a map of card types and counts"
  [card-map]
  (mapcat #(repeat (second %) (first %)) card-map))

(def default-deck
  "The default deck of cards: 15 gem cards + 3 each of 5 types of bad cards."
  (build-deck (zipmap (cons ::gem bad-cards) (cons 15 (repeat 3)))))

(defn end-of-game?
  "Is this game over?
  The game ends when two of the same bad cards are showing."
  [card card-map dealt-cards]
  (and (bad-card? card) (card-map card)))

(defn deal-round
  "Deal a round of cards, returning a vector of cards played."
  ([deck]
   (deal-round deck end-of-game?))
  ([deck end-of-game?]
   (let [cards (shuffle deck)]
     (loop [card-map {}
            dealt-cards []
            [card & rest-of-deck] cards]
       (cond
         ;; We're out of cards: return the whole deck.
         (nil? card)
         dealt-cards
         ;; Check for game over.
         (end-of-game? card card-map dealt-cards)
         (conj dealt-cards card)
         ;; Deal another card.
         :else
         (recur
           (update card-map card (fnil inc 0))
           (conj dealt-cards card)
           rest-of-deck))))))

