//
// The following are routing values that are checked for in the guiProps.routing field.
//

// Only sent locally within the client between components.
export const ROUTE_LOCAL = "local";

// Leaves the client and goes out on the network. In most cases when this is used you also want the "local" value,
// like "local,external".
export const ROUTE_EXTERNAL = "external";

// ----------------------------------------

