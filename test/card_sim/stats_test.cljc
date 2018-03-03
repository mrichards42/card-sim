(ns card-sim.stats-test (:require [clojure.test :refer :all]
            [card-sim.stats :as stats]))

(deftest mean-test
  (testing "single number"
    (is (= 2)
        (stats/mean [2])))
  (testing "two numbers"
      (is (= (/ 9 2))
          (stats/mean [2 7])))
  (testing "1 through 9"
      (is (= 5)
          (stats/mean [1 2 3 4 5 6 7 8 9])))
  (testing "same number"
      (is (= 5)
          (stats/mean [5 5 5 5]))))

(deftest freq-mean-test
  (testing "single number"
    (is (= 2)
        (stats/freq-mean (frequencies [2]))))
  (testing "two numbers"
      (is (= (/ 9 2))
          (stats/freq-mean (frequencies [2 7]))))
  (testing "1 through 9"
      (is (= 5)
          (stats/freq-mean (frequencies [1 2 3 4 5 6 7 8 9]))))
  (testing "same number"
      (is (= 5)
          (stats/freq-mean (frequencies [5 5 5 5])))))

(deftest median-test
  (testing "single number"
    (is (= 2)
        (stats/median [2])))
  (testing "two numbers"
      (is (= (/ 9 2))
          (stats/median [2 7])))
  (testing "odd count"
      (is (= 5)
          (stats/median [1 2 3 4 5 6 7 8 9])))
  (testing "even count"
      (is (= (/ 11 2))
          (stats/median [2 3 4 5 6 7 8 9])))
  (testing "even count, same number"
      (is (= 5)
          (stats/median [2 3 4 5 5 7 8 9])))
  (testing "same number"
      (is (= 5)
          (stats/median [5 5 5 5]))))

(deftest freq-median-test
  (testing "single number"
    (is (= 2)
        (stats/freq-median (frequencies [2]))))
  (testing "two numbers"
      (is (= (/ 9 2))
          (stats/freq-median (frequencies [2 7]))))
  (testing "odd count"
      (is (= 5)
          (stats/freq-median (frequencies [1 2 3 4 5 6 7 8 9]))))
  (testing "even count"
      (is (= (/ 11 2))
          (stats/freq-median (frequencies [2 3 4 5 6 7 8 9]))))
  (testing "even count, same number"
      (is (= 5)
          (stats/freq-median (frequencies [2 3 4 5 5 7 8 9]))))
  (testing "same number"
      (is (= 5)
          (stats/freq-median (frequencies [5 5 5 5])))))

