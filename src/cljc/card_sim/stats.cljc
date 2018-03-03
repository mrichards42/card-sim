(ns card-sim.stats
  "Basic stats functions.")

(defn mean
  "Compute the mean of a collection."
  [coll]
  (let [n (count coll)
        total (reduce + coll)]
    (/ total n)))

(defn median
  "Compute the median of a collection."
  [coll]
  (let [sorted (vec (sort coll))
        n (count sorted)
        mid (quot n 2)]
    (if (odd? n)
      (nth sorted mid)
      (/ (+ (nth sorted mid) (nth sorted (dec mid))) 2))))

(defn freq-mean
  "Compute the mean of a frequency map."
  [freqs]
  (let [n (reduce + (vals freqs))
        total (reduce + (map (partial apply *) freqs))]
    (/ total n)))

(defn freq-median
  "Compute the median of a frequency map.
  This is significantly faster for large datasets with repeated values."
  [freqs]
  (let [n (reduce + (vals freqs))
        mid (inc (quot n 2))]
    (loop [[[cur cur-count] & remaining] (sort freqs)
           prev cur
           prev-count 0]
      ;; Run through each bin in order, keeping track of where in the sequence
      ;; we are.  When we hit the mid point, we're done.
      (let [next-count (+ prev-count cur-count)]
        (if (< next-count mid)
          (recur remaining cur next-count)
          (cond
            ;; Odd means 1 middle value
            (odd? n) cur
            ;; Even means avg of 2 middle values.
            ;; If there are > 1 items in this bin, the 2 middle are the same
            (> 1 cur-count) cur
            ;; Otherwise it's the avg of this and the previous
            :else (/ (+ cur prev) 2)))))))

