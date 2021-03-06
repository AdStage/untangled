1.0.0-beta2
-----
- Added more form field types (html5-input supports all text-like html 5 input types)
- Improved options can be passed when rendering form fields (still needs more)
- Form commit now accepts a fallback
- Improved docstrings on form commit
- Added defvalidator for making form field validators
- Added untangled.client.data-fetch/fallback as the new name for tx/fallback. tx/fallback is still OK, but the new one uses a defmutation, so it pops up in IDEs.
- Added support for user-supplied client read handler

1.0.0-beta1
-----
- Fixed uses of clojure.spec to clojure.spec.alpha
- Integrated devguide
- Integrated cookbook (as demos)
- Removed local storage and async service stuff. It was undocumented, and unneeded.
- Removed `load-data` from data-fetch. Port to using `load` instead.
- Moved internal i18n vars to untangled.i18n namespace
- Removed protocol testing support. Port it from the old library into your project if you need it.
- Removed openid parsing (doesn't belong in this lib)
- Renamed untangled.client.impl.util to untangled.client.util
- Moved force-render and unique-key to untangled.client.util namespace.
- Moved strip-parameters to untangled.client.util (cljc)
- BREAKING CHANGE: Renamed built-in mutations using the defmutation macro.
   - untangled/load -> untangled.client.data-fetch/load
   - tx/fallback -> untangled.client.data-fetch/fallback
   - ui/change-locale -> untangled.client.mutations/change-locale
   - ui/set-props -> untangled.client.mutations/set-props
   - ui/toggle -> untangled.client.mutations/toggle
   Remember that namespace aliasing works within syntax quotes (e.g. `m/change-locale)
- Moved augment-capable defui to augmentation namespace. STILL EXPERIMENTAL.
- Merged server support into this project
- Moved easy untangled server to untangled.easy-server namespace. 
- Moved all other server code to untangled.server namespace.
- Added bootstrap3 namespace with helpers that can render passive and active bootstrap 3 elements.
- If `net/UntangledNetwork start` returns something that implements `UntangledNetwork`, this value will be used as the actual remote by the app.
- Made forms support work (initial render) for SSR
- MOVED pathopt option. It is now just one of the regular reconciler options you can pass. Currently does not default to true due to bugs in Om Next.

0.8.2
-----
- Changed network result handling so that it does not change :ui/react-key (flicker)
- Added support for splitting mutation txes when there are duplicate calls, so that they go over separate network requests to work around Om returning a map.

0.8.1
-----
- Fixed load markers for ident-based loading: They will appear iff the entity is already present (refresh)
- Cleaned up logic around data markers related to markers and data targeting
- Added sequential processing as a configurable option on networking
- Added devcards for integration testing loading cases
- Removed explicit require of devcards.core in devcard untangled-app macro so that devcards is not a hard dependency.
- Updated load to auto-add target or kw of load to the refresh list.

0.8.0
-----
- Added defmutation 
- Fixed up namespaces that defined macros to allow for implicit macro usage by adding self-references
- Added support for multiple remotes (networking option now accepts a map)
   - NOTE: `clear-pending-remote-requests!` now requires a remote parameter.
- Added support for progressive load updates (nice for file
upload support)

0.7.0
-----
- Removed cache 
- Added UI routing helpers

0.6.1
-----
- Added support for nil as subquery class in load
- Fixed preprocess-merge to eliminate litter in app state
- Added support for server-side rendering.
- Removed forced root re-render on post mutations. POTENTIALLY BREAKING CHANGE!
   - The intended use is to include :refresh with your loads that indicate what to re-render
- Fixed bug in InitialAppState that was missing the merge of nested unions on startup

0.6.0
-----
- Changed InitialAppState to overwrite any supplied initial app state atom.
  This allows you to inspect data (the app state) when embedding an Untangled application in a devcard.
- Added new `load` and `load-action` functions with cleaner interface. Deprecated `load-data` and `load-data-action`.
    - Now have the ability to target a top-level query to a spot in app state. Reduces need for post mutations
    - Reduced arguments for better clarity
    - Added ability to pass untangled app, so that use in started-callback is easier.
- Fixed bug in failed loading markers
- Fixed bug with removal/addition of markers when markers are off
- Added jump to and playback speed features to the support viewer.
- Added support for post-mutation parameters in load API.
- Added support for custom handling of merge of return values from server mutations (see `:mutation-merge`
  in `new-untangled-client`).
- Added support for custom transit handlers on the client side. Server side is coming in a release soon.
- Added support for turning on/off Om path optimization
- Fix for latest cljs support (PR 47)
- DEPRECATED: load-data will soon no longer supports the :ident parameter. Use load instead.

0.5.7
-----
- The `:marker` keyword actually works now!
- Fix: data fetch with parameters places the load marker in the correct location in app state
- Fix: error callback doesn't attempt to modify data state in app state db when the data state's marker is false

0.5.6
-----
- Fixed bug with global-error-callback not being called with a server error returns no body
- Fixed bug that prevents error processing if the server is completely down
- Added reset-history! function to reset UntangledApplication Om cache history.
- Added to UntangledApplication protocol:
  * (reset-app! [this root-component callback] "Replace the entire app state with the initial app state defined on the root component (includes auto-merging of unions). callback can be nil, a function, or :original (to call original started-callback).")
  * (clear-pending-remote-requests! [this] "Remove all pending network requests. Useful on failures to eliminate cascading failures.")

0.5.5
-----
- Fixed bug where keywords in a union query were not elided when specified in the `:without` set of data fetches
- Fixed bug with query combining that was causing parallel reads to collide
- Corrected initialization order so that alternates on unions are done before startup callback
- Fixed a rendering refresh bug on post mutations
- Fixed compiler warnings about clojure walk

0.5.4
-----
- Added marker option to loads, so that load markers are optional
- OpenID client will now extract tokens from cookies as well as the header.

0.5.3
-----
- Added utility function integrate-ident!
- Refined merge-state!
- Renamed Constructor to InitialAppState
- Automated initialization of to-one unions (to-many was already doable by Om)

0.5.2
-----
- Fixed bug with initial state and new constructors

0.5.1
-----
- Added untangled/Constructor for adding initial state to UI components
- Added merge-state! for easily merging component-centric data (e.g. from server push)

0.5.0
------
- Significant optimizations to post-query processing.
- BREAKING CHANGE: to load-data. You should now include :refresh to trigger re-rendering of components. This removes the
  internal need for a forced root re-render. Proper refresh after load-data now requires this parameter.
- Removed deprecated load-collection and load-singleton. Use load-data instead (name change only)

0.4.9
-----
- Removed old logging code (use untangled.client.logging instead)
- Added support for parallel lazy loading
- Added `(start [this app])` to the `UntangledNetwork` protocol.
- Log-app-state now requires the atom containing a mounted untangled client, define it in the user namespace like so:
```
(defonce app (atom (uc/new-untangled-client ... )))
(swap! app uc/mount RootComponent "app-div")
(def log-app-state (partial util/log-app-state app))
```
- `global-error-callback` now expectes an arity 2 function. First param is the status and the second is the response.
- Fixed bug that closed over tempids in network callbacks
- Fixed bug in path-optimized union query parsing

0.4.8
-----
- Fixed tempid rewrite regression

0.4.7
-----
- Upgraded to Om-alpha32
- untangled.openid-client/setup, parses any openid claims from the webtoken in the url's hash fragments
- Renamed load-collection/singleton to load-data. Old names are deprecated, but not yet removed.
- Renamed :app/loading-data to :ui/loading-data
- Added remote trigger method for doing loading from mutations.
- Renamed everything in the internals that was prefixed app/ to untangled/
- Added global server error handler.
- Added fallback support for load-data, load-field, etc.
- Refactored networking send for better clarity
- Fixed bug on mark/sweep of missing query results. It was being applied to mutations instead of reads. Added tests for this that need a bit more work.

0.4.6
-----
- Fixed local read bug, and turned on path optimization
- Fixed fallback handling of failed remote transactions, and added lots of tests

0.4.5
-----
- Renamed react-key to ui/react-key
- Renamed app/locale to ui/locale
- Renamed mutation app/change-locale to ui/change-locale
- Implemented a number of missing things in i18n
- Removed a number of i18n helpers that were redundant to trf
- Added :request-transform networking hook with spec
- Modified load callback to a mutation symbol
- Changed :params of loaders to allow you to specify which prop on the query gets the stated parameters
- Added history method to application protocol, to make implementing history viewer in an app trivial

