(ns advent-2019.day16
  (:require [advent-2019.core :refer [parse-int]])
  (:require [clojure.string :refer [join]]))

(def input "59717513948900379305109702352254961099291386881456676203556183151524797037683068791860532352118123252250974130706958763348105389034831381607519427872819735052750376719383812473081415096360867340158428371353702640632449827967163188043812193288449328058464005995046093112575926165337330100634707115160053682715014464686531460025493602539343245166620098362467196933484413717749680188294435582266877493265037758875197256932099061961217414581388227153472347319505899534413848174322474743198535953826086266146686256066319093589456135923631361106367290236939056758783671975582829257390514211329195992209734175732361974503874578275698611819911236908050184158")

; (def input "03036732577212944063491565474664")

(def signal (map #(- (int %) (int \0)) input))

(defn apply-pattern
  [s1 s2]
  (Math/abs 
   (rem (->> (list s1 s2)
             (apply map *)
             (reduce +)
             )
        10)))

(defn create-pattern
  [n]
  (->> [0 1 0 -1]
       (mapcat #(repeat (+ n 1) %))
       (cycle)
       (drop 1)))

(def patterns
  (map create-pattern (range)))

(defn phase
  [signal]
  (take (count signal)
        (map (partial apply-pattern signal) patterns)))

(def part1
  (nth (iterate phase signal) 100))

(def signal-index (parse-int (subs input 0 7)))

(def repeated-signal (take (* (count signal) 10000) (cycle signal)))

; the bottom half of the matrix of a single transform is a triangle with only 1s in it.
; this means that you can add the digits modulo 10 from back to front in each phase
(defn phase-2
  [signal]
  (reductions
   (fn [sofar x] (mod (+ x sofar) 10))
   signal))

(defn transform-2
  [signal]
  (join (reverse (nth (iterate phase-2 (reverse signal)) 100))))

(defn -main [& _] (println "day16" (subs (transform-2 repeated-signal) signal-index (+ signal-index 8))))