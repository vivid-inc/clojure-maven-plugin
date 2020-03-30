;; Copyright 2020 The vivid:clojure-maven-plugin Authors
;;
;; Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
;; in compliance with the License. You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software distributed under the License
;; is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
;; or implied. See the License for the specific language governing permissions and limitations under
;; the License.

(ns hello-world)

(defn -main [& _]
      (spit "hakidasu.txt" "Hello World\n")
      (println "wrote hakidasu.txt"))
