(ns vivid.cmp.leiningen
  (:require
    [leiningen.core.main :as lein]))

(defn- exit
  ([exit-code & msg]
   (when-not (= exit-code 0)
     (throw (ex-info (if (seq msg)
                       (apply print-str msg)
                       "Suppressed exit")
                     {:exit-code exit-code :suppress-msg (empty? msg)}))))
  ([] (exit 0)))

(defn lein-main [debug? args]
  (let [raw-args (re-seq #"[^\s]+" args)]
    (when debug?
      (alter-var-root #'lein/*debug* (constantly true)))
    (alter-var-root #'lein/*exit-process?* (constantly false))
    (with-redefs [lein/exit exit]
      (apply lein/-main raw-args))))
