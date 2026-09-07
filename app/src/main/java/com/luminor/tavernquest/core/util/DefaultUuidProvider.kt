package com.luminor.tavernquest.core.util
import java.util.UUID
class DefaultUuidProvider:UuidProvider{override fun newId()=UUID.randomUUID().toString()}
