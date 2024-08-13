package net.gradleutil.conf.transform.schema

import com.networknt.schema.ValidationMessage
import com.networknt.schema.walk.JsonSchemaWalkListener
import com.networknt.schema.walk.WalkEvent
import com.networknt.schema.walk.WalkFlow

class ExamplePropertyWalkListener implements JsonSchemaWalkListener {
    @Override
    WalkFlow onWalkStart(WalkEvent walkEvent) {
        println walkEvent.node.fieldNames().toString()
        return null
    }

    @Override
    void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> set) {

    }
}
