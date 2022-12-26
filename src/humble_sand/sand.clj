(ns humble-sand.sand
  (:require [clojure.string :as string]))

(def sand-sleep-ms 100)

(def running true)

(def cave-system (ref {}))
(def falling-sand (ref nil))

(defn parse-input [input]
  (->> input
       string/split-lines
       (map (comp
             (partial partition 2 1)
             (partial partition 2)
             (partial map parse-long)
             #(string/split % #"\D+")))
       (apply concat)))

(defn build-wall [[[x y] [X Y]]]
  (cond
    (< y Y) (for [yr (range y (inc Y))]
              [x yr])
    (< x X) (for [xr (range x (inc X))]
              [xr y])
    (> y Y) (for [yr (range y (dec Y) -1)]
              [x yr])
    (> x X) (for [xr (range x (dec X) -1)]
              [xr y])))

(defn setup
  "places rock walls"
  [input]
  (dosync
   (ref-set falling-sand [500 0])
   (ref-set cave-system (assoc (zipmap (->> input
                                            parse-input
                                            (map build-wall)
                                            (apply concat))
                                       (repeat :rock))
                               [500 0]
                               :sand))))

(defn drop-sand []
  (dosync
   (let [[x y]       @falling-sand
         down?       (nil? (get @cave-system [x (inc y)]))
         down-left?  (nil? (get @cave-system [(dec x) (inc y)]))
         down-right? (nil? (get @cave-system [(inc x) (inc y)]))]
     (cond
       down?       (do (ref-set falling-sand [x (inc y)])
                       (alter cave-system dissoc [x y])
                       (alter cave-system assoc [x (inc y)] :sand))
       down-left?  (do (ref-set falling-sand [(dec x) (inc y)])
                       (alter cave-system dissoc [x y])
                       (alter cave-system assoc [(dec x) (inc y)] :sand))
       down-right? (do (ref-set falling-sand [(inc x) (inc y)])
                       (alter cave-system dissoc [x y])
                       (alter cave-system assoc [(inc x) (inc y)] :sand))
       :else       (do (ref-set falling-sand [500 0])
                       (alter cave-system assoc [500 0] :sand)))))
  )

(def dropper (agent nil))

(defn sand-falling [x]
  (when running
    (send-off *agent* #'sand-falling))
  (drop-sand)
  (. Thread (sleep sand-sleep-ms))
  nil)
