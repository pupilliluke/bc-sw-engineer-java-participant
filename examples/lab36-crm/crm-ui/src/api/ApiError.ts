export type ApiErrorKind = 'network' | 'http' | 'abort' | 'parse'

export class ApiError extends Error {
  readonly kind: ApiErrorKind
  readonly status?: number
  readonly fieldErrors?: Record<string, string>

  constructor(
    message: string,
    kind: ApiErrorKind,
    status?: number,
    fieldErrors?: Record<string, string>
  ) {
    super(message)
    this.name = 'ApiError'
    this.kind = kind
    this.status = status
    this.fieldErrors = fieldErrors
  }

  static async from(response: Response): Promise<ApiError> {
    let message = `HTTP ${response.status}`
    let fieldErrors: Record<string, string> | undefined
    try {
      const body = await response.json()
      if (typeof body?.message === 'string') {
        message = body.message
      } else if (typeof body?.error === 'string') {
        message = body.error
      }
      if (body?.fieldErrors && typeof body.fieldErrors === 'object') {
        fieldErrors = body.fieldErrors as Record<string, string>
      }
    } catch {}
    return new ApiError(message, 'http', response.status, fieldErrors)
  }
}

export class ForbiddenError extends ApiError {
  constructor(message: string) {
    super(message, 'http', 403)
    this.name = 'ForbiddenError'
  }
}
