type AuthRouteAccessName = 'CodeLoginVerify' | 'MfaChallenge'

const AUTH_ROUTE_ACCESS_PREFIX = 'velox:auth-route-access:'

const getAccessKey = (name: AuthRouteAccessName) => `${AUTH_ROUTE_ACCESS_PREFIX}${name}`

export function grantAuthRouteAccess(name: AuthRouteAccessName): void {
  sessionStorage.setItem(getAccessKey(name), '1')
}

export function consumeAuthRouteAccess(name: AuthRouteAccessName): boolean {
  const key = getAccessKey(name)
  const hasAccess = sessionStorage.getItem(key) === '1'
  sessionStorage.removeItem(key)
  return hasAccess
}
