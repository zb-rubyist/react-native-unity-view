#import "RCTViewManager.h"
#import <UnityFramework/UnityFramework.h>
#import "UnityUtils.h"
#import "RNUnityView.h"

@interface RNUnityViewManager : RCTViewManager<UnityFrameworkListener>

@property (nonatomic, strong) RNUnityView *currentView;

@end
