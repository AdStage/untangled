# 0.3.3
- PR #5 - Added ability to add read and write transit handlers to the default set.
- Fixed dependencies from provided to test for spec

# 0.3.2
- Added the ability to define the host for `make-channel-client`.

# 0.3.1
- Fix initialization timing issue where websockets messages were being received before multimethods were installed.

# 0.3.0
- Fix Issue #2 - use promise channel to validate initilization of channel in send protocol method.
- Bumped Sente to version "1.10.0" and Timbre to "4.7.3". These versions, are dependent on `com.taoensso/encore 2.67.1`.

# 0.2.2
- Fix Issue #1 - `global-error-callback` is no longer stored in an atom.

# 0.2.1
- `state-callback`, a function of arity 2, can now be passed to the network. This can be used to execute actions based on the websockets state change.
- Reconnecting is now possible through the `ChannelSocket` protocol's `reconnect` function.
- Add validation for ws connection origins.
- Add validation for client ids upon http GET request. This is a place for potential XSS.

# 0.2.0
- `global-error-callback` is now a arity 2 function. It takes status and body, in that order. Refer to github issue [untangled-web/untangled-client#14](https://github.com/untangled-web/untangled-client/issues/14).
- Uses untangled-server 0.5.1 and untangled-client 0.5.0.

# 0.1.0
- Initial release
