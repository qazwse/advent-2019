(ns advent-2019.day05
  (:require [clojure.math.numeric-tower :refer [expt]]))

(def input
  [3,225,1,225,6,6,1100,1,238,225,104,0,101,67,166,224,1001,224,-110,224,4,224,102,8,223,223,1001,224,4,224,1,224,223,223,2,62,66,224,101,-406,224,224,4,224,102,8,223,223,101,3,224,224,1,224,223,223,1101,76,51,225,1101,51,29,225,1102,57,14,225,1102,64,48,224,1001,224,-3072,224,4,224,102,8,223,223,1001,224,1,224,1,224,223,223,1001,217,90,224,1001,224,-101,224,4,224,1002,223,8,223,1001,224,2,224,1,223,224,223,1101,57,55,224,1001,224,-112,224,4,224,102,8,223,223,1001,224,7,224,1,223,224,223,1102,5,62,225,1102,49,68,225,102,40,140,224,101,-2720,224,224,4,224,1002,223,8,223,1001,224,4,224,1,223,224,223,1101,92,43,225,1101,93,21,225,1002,170,31,224,101,-651,224,224,4,224,102,8,223,223,101,4,224,224,1,223,224,223,1,136,57,224,1001,224,-138,224,4,224,102,8,223,223,101,2,224,224,1,223,224,223,1102,11,85,225,4,223,99,0,0,0,677,0,0,0,0,0,0,0,0,0,0,0,1105,0,99999,1105,227,247,1105,1,99999,1005,227,99999,1005,0,256,1105,1,99999,1106,227,99999,1106,0,265,1105,1,99999,1006,0,99999,1006,227,274,1105,1,99999,1105,1,280,1105,1,99999,1,225,225,225,1101,294,0,0,105,1,0,1105,1,99999,1106,0,300,1105,1,99999,1,225,225,225,1101,314,0,0,106,0,0,1105,1,99999,1107,226,226,224,102,2,223,223,1006,224,329,1001,223,1,223,1007,226,677,224,1002,223,2,223,1005,224,344,101,1,223,223,108,677,677,224,1002,223,2,223,1006,224,359,101,1,223,223,1008,226,226,224,1002,223,2,223,1005,224,374,1001,223,1,223,108,677,226,224,1002,223,2,223,1006,224,389,101,1,223,223,7,226,226,224,102,2,223,223,1006,224,404,101,1,223,223,7,677,226,224,1002,223,2,223,1005,224,419,101,1,223,223,107,226,226,224,102,2,223,223,1006,224,434,1001,223,1,223,1008,677,677,224,1002,223,2,223,1005,224,449,101,1,223,223,108,226,226,224,102,2,223,223,1005,224,464,1001,223,1,223,1108,226,677,224,1002,223,2,223,1005,224,479,1001,223,1,223,8,677,226,224,102,2,223,223,1006,224,494,1001,223,1,223,1108,677,677,224,102,2,223,223,1006,224,509,1001,223,1,223,1007,226,226,224,1002,223,2,223,1005,224,524,1001,223,1,223,7,226,677,224,1002,223,2,223,1005,224,539,1001,223,1,223,8,677,677,224,102,2,223,223,1005,224,554,1001,223,1,223,107,226,677,224,1002,223,2,223,1006,224,569,101,1,223,223,1107,226,677,224,102,2,223,223,1005,224,584,1001,223,1,223,1108,677,226,224,102,2,223,223,1006,224,599,1001,223,1,223,1008,677,226,224,102,2,223,223,1006,224,614,101,1,223,223,107,677,677,224,102,2,223,223,1006,224,629,1001,223,1,223,1107,677,226,224,1002,223,2,223,1005,224,644,101,1,223,223,8,226,677,224,102,2,223,223,1005,224,659,1001,223,1,223,1007,677,677,224,102,2,223,223,1005,224,674,1001,223,1,223,4,223,99,226])

(defn opcode [instruction] (mod instruction 100))
(defn parameter-mode [instruction index] (rem (quot instruction (expt 10 (+ index 2))) 10))
(defn operand-value [operand mode program] (if (= mode 0) (get program operand) operand))

(defn halt [program] (get program 0))

(defn add-mult
  [program, ip]
  (let [[instruction op1 op2 target] (subvec program ip)
        value1 (operand-value op1 (parameter-mode instruction 0) program)
        value2 (operand-value op2 (parameter-mode instruction 1) program)]
    (case (opcode instruction)
      1 [(assoc program target (+ value1 value2)) (+ ip 4)]
      2 [(assoc program target (* value1 value2)) (+ ip 4)])))

(defn read-input
  [program, ip, sys-id]
  (let [[_ target] (subvec program ip)]
    [(assoc program target sys-id) (+ ip 2)]))

(defn do-output
  [program, ip]
  (let [[instruction op] (subvec program ip)
        value (operand-value op (parameter-mode instruction 0) program)]
    (println ">" value)
    [program (+ ip 2)]))

(defn jmp
  [program, ip]
  (let [[instruction op1 op2] (subvec program ip)
        value1 (operand-value op1 (parameter-mode instruction 0) program)
        value2 (operand-value op2 (parameter-mode instruction 1) program)
        do-jump (case (opcode instruction)
                  5 (not= value1 0)
                  6 (= value1 0))]
    [program (if do-jump value2 (+ ip 3))]))

(defn cmp
  [program, ip]
  (let [[instruction op1 op2 target] (subvec program ip)
        value1 (operand-value op1 (parameter-mode instruction 0) program)
        value2 (operand-value op2 (parameter-mode instruction 1) program)]
    (case (opcode instruction)
      7 [(assoc program target (if (< value1 value2) 1 0)) (+ ip 4)]
      8 [(assoc program target (if (= value1 value2) 1 0)) (+ ip 4)])))

(defn run
  "Runs the program to completion"
  [p0, ip0, sys-id]
  (loop [state [p0 ip0]]
    (let [[program ip] state]
      (case (opcode (get program ip))
        99 (halt program)
        1 (recur (add-mult program ip))
        2 (recur (add-mult program ip))
        3 (recur (read-input program ip sys-id))
        4 (recur (do-output program ip))
        5 (recur (jmp program ip))
        6 (recur (jmp program ip))
        7 (recur (cmp program ip))
        8 (recur (cmp program ip))))))

(defn -main [& args]
  (run input 0 1)
  (run input 0 5))