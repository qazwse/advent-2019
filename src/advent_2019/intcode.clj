(ns advent-2019.intcode
  (:require [clojure.string :refer [join]]))

(defn opcode [instruction] (mod instruction 100))
(defn parameter-mode [instruction index] (rem (quot instruction (nth [100 1000 10000] index)) 10))
(defn target-value [target mode program]
  (if (not= mode 2) target (+ target (program :rb))))
(defn operand-value [operand mode program]
  (let [value (case mode
                0 (program operand)
                1 operand
                2 (program (+ operand (program :rb))))]
    (if (nil? value) 0 value)))

(defn compute-and-set
  [program, ip]
  (let [instruction (program ip)
        op1 (program (+ ip 1))
        op2 (program (+ ip 2))
        target (program (+ ip 3))
        value1 (operand-value op1 (parameter-mode instruction 0) program)
        value2 (operand-value op2 (parameter-mode instruction 1) program)
        target-to-set (target-value target (parameter-mode instruction 2) program)
        value-to-set (case (opcode instruction)
                       1 (+ value1 value2)
                       2 (* value1 value2)
                       7 (if (< value1 value2) 1 0)
                       8 (if (= value1 value2) 1 0))]
    [(assoc program target-to-set value-to-set) (+ ip 4)]))

(defn adjust-relative-base
  [program, ip]
  (let [instruction (program ip)
        op (program (+ ip 1))
        value (operand-value op (parameter-mode instruction 0) program)]
    [(assoc program :rb (+ value (program :rb))) (+ ip 2)]))

(defn read-input
  [program, ip, value]
  (let [instruction (program ip)
        target (program (+ ip 1))
        target-to-set (target-value target (parameter-mode instruction 0) program)]
    [(assoc program target-to-set value) (+ ip 2)]))

(defn do-output
  [program, ip]
  (let [instruction (program ip)
        op (program (+ ip 1))
        value (operand-value op (parameter-mode instruction 0) program)]
    [[program (+ ip 2)] value]))

(defn jmp
  [program, ip]
  (let [instruction (program ip)
        op1 (program (+ ip 1))
        op2 (program (+ ip 2))
        value1 (operand-value op1 (parameter-mode instruction 0) program)
        value2 (operand-value op2 (parameter-mode instruction 1) program)
        do-jump (case (opcode instruction)
                  5 (not= value1 0)
                  6 (= value1 0))]
    [program (if do-jump value2 (+ ip 3))]))

(defn run
  "Runs the program until it needs more input or halts"
  [state0, input0]
  (loop [state state0 inputs input0 output []]
    (let [[program ip] state]
      (case (opcode (program ip))
        99 [nil output]
        1 (recur (compute-and-set program ip) inputs output)
        2 (recur (compute-and-set program ip) inputs output)
        3 (if (empty? inputs) [[program ip] output]
              (recur (read-input program ip (first inputs)) (rest inputs) output))
        4 (let [[new-state new-output] (do-output program ip)]
            (recur new-state inputs (conj output new-output)))
        5 (recur (jmp program ip) inputs output)
        6 (recur (jmp program ip) inputs output)
        7 (recur (compute-and-set program ip) inputs output)
        8 (recur (compute-and-set program ip) inputs output)
        9 (recur (adjust-relative-base program ip) inputs output)))))

(defn print-ascii-output
  [output]
  (join (map #(char %) output)))