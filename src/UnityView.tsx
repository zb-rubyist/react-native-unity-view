import * as React from 'react'
import { NativeModules, requireNativeComponent, View, ViewProps, ViewPropTypes } from 'react-native'
import * as PropTypes from 'prop-types'
import MessageHandler from './MessageHandler'
import { ListenerHandle, UnityModule, UnityViewMessage } from './UnityModule'
import { Component, useEffect, useState } from 'react'

const { UIManager, UnityViewUtil } = NativeModules

interface UnityViewCustomParams {
    [key: string]: string | number | boolean
}

export interface UnityViewProps extends ViewProps {
    /**
     * Receive string message from unity.
     */
    onMessage?: (message: string) => void;
    /**
     * Receive unity message from unity.
     */
    onUnityMessage?: (handler: MessageHandler) => void;

    onUnload?: () => void;

    children?: React.ReactNode;
    
    customParams?: UnityViewCustomParams
}

let NativeUnityView

class UnityView extends Component<UnityViewProps> {

    state = {
        handle: null
    }
    unloadEventHandle: ListenerHandle

    componentDidMount(): void {
        const { onUnityMessage, onMessage, onUnload, customParams } = this.props
        this.unloadEventHandle = UnityModule.addUnityUnloadListener(() => {
            onUnload()
        })
        this.setState({
            handle: UnityModule.addMessageListener(message => {
                if (onUnityMessage && message instanceof MessageHandler) {
                    onUnityMessage(message)
                }
                if (onMessage && typeof message === 'string') {
                    onMessage(message)
                }
            })
        })
        
        UnityViewUtil.setLaunchOptions(customParams)
    }

    componentWillUnmount(): void {
        if (this.unloadEventHandle)
            this.unloadEventHandle.release();

        UnityModule.removeMessageListener(this.state.handle)
        UnityModule.pause()
    }

    render() {
        const { props } = this
        return (
            <View {...props}>
            <NativeUnityView
                style={{ position: 'absolute', left: 0, right: 0, top: 0, bottom: 0 }}
                onUnityMessage={props.onUnityMessage}
                onMessage={props.onMessage}
            >
            </NativeUnityView>
            {props.children}
        </View>
        )
    }
}
/*
const UnityView = ({ onUnityMessage, onMessage, ...props } : UnityViewProps) => {
    const [handle, setHandle] = useState(null)

    useEffect(() => {
        setHandle(UnityModule.addMessageListener(message => {
            if (onUnityMessage && message instanceof MessageHandler) {
                onUnityMessage(message)
            }
            if (onMessage && typeof message === 'string') {
                onMessage(message)
            }
        }))
        return () => {
            UnityModule.removeMessageListener(handle)
        }
    }, [onUnityMessage, onMessage, handle, setHandle])

    return (
        <View {...props}>
            <NativeUnityView
                style={{ position: 'absolute', left: 0, right: 0, top: 0, bottom: 0 }}
                onUnityMessage={onUnityMessage}
                onMessage={onMessage}
            >
            </NativeUnityView>
            {props.children}
        </View>
    )
}
*/

NativeUnityView = requireNativeComponent('RNUnityView', UnityView)

export default UnityView;
