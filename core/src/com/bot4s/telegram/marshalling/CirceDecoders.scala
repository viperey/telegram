package com.bot4s.telegram.marshalling

import java.util.NoSuchElementException
import com.bot4s.telegram.methods.ChatAction.ChatAction
import com.bot4s.telegram.methods.ParseMode.ParseMode
import com.bot4s.telegram.methods.PollType.PollType
import com.bot4s.telegram.models.*
import com.bot4s.telegram.methods.*
import com.bot4s.telegram.methods.{ ChatAction, ParseMode, PollType, Response }
import com.bot4s.telegram.models.ChatType.ChatType
import com.bot4s.telegram.models.CountryCode.CountryCode
import com.bot4s.telegram.models.Currency.Currency
import com.bot4s.telegram.models.MaskPositionType.MaskPositionType
import com.bot4s.telegram.models.MemberStatus.MemberStatus
import com.bot4s.telegram.models.BotCommandScope.BotCommandScope
import com.bot4s.telegram.models.MessageEntityType.MessageEntityType
import com.bot4s.telegram.models.StickerType.StickerType
import com.bot4s.telegram.models.StickerFormat.StickerFormat
import UpdateType.UpdateType
import com.bot4s.telegram.models.*
import io.circe.{ Decoder, HCursor }
import io.circe.generic.extras.semiauto.*
import com.typesafe.scalalogging.StrictLogging
import io.circe.Decoder.decodeInt
import io.circe.Decoder.decodeSeq
import io.circe.Decoder.decodeArray
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.semiauto.deriveDecoder

/**
 * Circe marshalling borrowed/inspired from [[https://github.com/nikdon/telepooz]]
 */
trait CirceDecoders extends StrictLogging {

  // Add explicit decoders for primitive types that Scala 3 might have trouble with
  implicit val booleanDecoder: Decoder[Boolean] = Decoder.decodeBoolean
  implicit val stringDecoder: Decoder[String]   = Decoder.decodeString
  implicit val config: Configuration = Configuration.default
    .withDiscriminator("type")
    .copy(transformConstructorNames = {
      case "InputMediaPhoto"     => "photo"
      case "InputMediaVideo"     => "video"
      case "InputMediaAudio"     => "audio"
      case "InputMediaDocument"  => "document"
      case "InputMediaAnimation" => "animation"
    })

  implicit def eitherDecoder[A, B](implicit decA: Decoder[A], decB: Decoder[B]): Decoder[Either[A, B]] = {
    val l: Decoder[Either[A, B]] = decA.map(Left.apply)
    val r: Decoder[Either[A, B]] = decB.map(Right.apply)
    l or r
  }

  implicit val createInvoiceLinkDecoder: Decoder[CreateInvoiceLink] = deriveDecoder[CreateInvoiceLink]

  implicit val botCommandScopeDecoder: Decoder[BotCommandScope] =
    Decoder[String].map(s => BotCommandScope.withName(pascalize(s)))
  implicit val memberStatusDecoder: Decoder[MemberStatus] =
    Decoder[String].map(s => MemberStatus.withName(pascalize(s)))
  implicit val maskPositionTypeDecoder: Decoder[MaskPositionType] =
    Decoder[String].map(s => MaskPositionType.withName(pascalize(s)))

  implicit val chatTypeDecoder: Decoder[ChatType] =
    Decoder[String].map(s => ChatType.withName(pascalize(s)))

  implicit val messageEntityTypeDecoder: Decoder[MessageEntityType] =
    Decoder[String].map { s =>
      try {
        MessageEntityType.withName(pascalize(s))
      } catch {
        case e: NoSuchElementException =>
          logger.warn(s"Unexpected MessageEntityType: '$s', fallback to Unknown.")
          MessageEntityType.Unknown
      }
    }

  implicit val stickerTypeDecoder: Decoder[StickerType] =
    Decoder[String].map { s =>
      try {
        StickerType.withName(pascalize(s))
      } catch {
        case e: NoSuchElementException =>
          logger.warn(s"Unexpected StickerType: '$s', fallback to Unknown.")
          StickerType.Unknown
      }
    }

  implicit val parseModeDecoder: Decoder[ParseMode] =
    Decoder[String].map(s => ParseMode.withName(pascalize(s)))

  implicit val pollTypeDecoder: Decoder[PollType] =
    Decoder[String].map(s => PollType.withName(pascalize(s)))

  implicit val countryCodeDecoder: Decoder[CountryCode] =
    Decoder[String].map(a => CountryCode.withName(a))

  implicit val currencyDecoder: Decoder[Currency] =
    Decoder[String].map(a => Currency.withName(a))

  implicit val chatIdDecoder: Decoder[ChatId] =
    Decoder[String].map(ChatId.Channel.apply) or Decoder[Long].map(ChatId.Chat.apply)

  implicit val chatActionDecoder: Decoder[ChatAction] =
    Decoder[String].map(s => ChatAction.withName(pascalize(s)))

  implicit val updateTypeDecoder: Decoder[UpdateType] =
    Decoder[String].map(s => UpdateType.withName(pascalize(s)))

  implicit val listBotCommandDecoder: Decoder[List[BotCommand]] = deriveDecoder[List[BotCommand]]
  implicit val botCommandDecoder: Decoder[BotCommand]           = deriveDecoder[BotCommand]

  implicit val chatLocationDecoder: Decoder[ChatLocation] = deriveDecoder[ChatLocation]
  // for v6.7 support
  implicit val botNameDecoder: Decoder[BotName] = deriveDecoder[BotName]
  implicit val inlineQueryResultsButtonDecoder: Decoder[InlineQueryResultsButton] =
    deriveDecoder[InlineQueryResultsButton]
  implicit val switchInlineQueryChosenChatDecoder: Decoder[SwitchInlineQueryChosenChat] =
    deriveDecoder[SwitchInlineQueryChosenChat]
  // for v6.6 support
  implicit val stickerFormatDecoder: Decoder[StickerFormat] =
    Decoder[String].map(s => StickerFormat.withName(pascalize(s)))

  implicit val botDescriptionDecoder: Decoder[BotDescription]           = deriveDecoder[BotDescription]
  implicit val botShortDescriptionDecoder: Decoder[BotShortDescription] = deriveDecoder[BotShortDescription]
  // for v6.5 support
  implicit val KeyboardButtonRequestUserDecoder: Decoder[KeyboardButtonRequestUser] =
    deriveDecoder[KeyboardButtonRequestUser]
  implicit val KeyboardButtonRequestChatDecoder: Decoder[KeyboardButtonRequestChat] =
    deriveDecoder[KeyboardButtonRequestChat]
  implicit val UserSharedDecoder: Decoder[UserShared] = deriveDecoder[UserShared]
  implicit val ChatSharedDecoder: Decoder[ChatShared] = deriveDecoder[ChatShared]

  // for v6.4 support
  implicit val editGeneralForumTopicDecoder: Decoder[EditGeneralForumTopic]     = deriveDecoder[EditGeneralForumTopic]
  implicit val editMessageCaptionDecoder: Decoder[EditMessageCaption]           = deriveDecoder[EditMessageCaption]
  implicit val EditMessageLiveLocationDecoder: Decoder[EditMessageLiveLocation] = deriveDecoder[EditMessageLiveLocation]

  implicit val inputFileDecoder: Decoder[InputFile]                           = Decoder.decodeString.map(InputFile(_))
  implicit val inputMediaPhotoDecoder: Decoder[InputMediaPhoto]               = deriveConfiguredDecoder
  implicit val inputMediaVideoDecoder: Decoder[InputMediaVideo]               = deriveConfiguredDecoder
  implicit val inputMediaAudioDecoder: Decoder[InputMediaAudio]               = deriveConfiguredDecoder
  implicit val inputMediaDocumentDecoder: Decoder[InputMediaDocument]         = deriveConfiguredDecoder
  implicit val inputMediaAnimationDecoder: Decoder[InputMediaAnimation]       = deriveConfiguredDecoder
  implicit val inputMediaDecoder: Decoder[InputMedia]                         = deriveConfiguredDecoder
  implicit val EditMessageMediaDecoder: Decoder[EditMessageMedia]             = deriveDecoder[EditMessageMedia]
  implicit val EditMessageReplyMarkupDecoder: Decoder[EditMessageReplyMarkup] = deriveDecoder[EditMessageReplyMarkup]
  implicit val EditMessageTextDecoder: Decoder[EditMessageText]               = deriveDecoder[EditMessageText]

  implicit val closeGeneralForumTopicDecoder: Decoder[CloseGeneralForumTopic]   = deriveDecoder[CloseGeneralForumTopic]
  implicit val reopenGeneralForumTopicDecoder: Decoder[ReopenGeneralForumTopic] = deriveDecoder[ReopenGeneralForumTopic]
  implicit val hideGeneralForumTopicDecoder: Decoder[HideGeneralForumTopic]     = deriveDecoder[HideGeneralForumTopic]
  implicit val unhideGeneralForumTopicDecoder: Decoder[UnhideGeneralForumTopic] = deriveDecoder[UnhideGeneralForumTopic]
  implicit val generalForumTopicHiddenDecoder: Decoder[GeneralForumTopicHidden.type] =
    deriveDecoder[GeneralForumTopicHidden.type]
  implicit val generalForumTopicUnhiddenDecoder: Decoder[GeneralForumTopicUnhidden.type] =
    deriveDecoder[GeneralForumTopicUnhidden.type]
  implicit val maybeWriteAccessAllowedDecoder: Decoder[Option[WriteAccessAllowed]] =
    Decoder.decodeOption[WriteAccessAllowed]
  implicit val writeAccessAllowedDecoder: Decoder[WriteAccessAllowed] = deriveDecoder[WriteAccessAllowed]

  // for v6.3 support
  implicit val forumTopicDecoder: Decoder[ForumTopic]                      = deriveDecoder[ForumTopic]
  implicit val forumTopicCreatedDecoder: Decoder[ForumTopicCreated]        = deriveDecoder[ForumTopicCreated]
  implicit val forumTopicClosedDecoder: Decoder[ForumTopicClosed.type]     = deriveDecoder[ForumTopicClosed.type]
  implicit val forumTopicReopenedDecoder: Decoder[ForumTopicReopened.type] = deriveDecoder[ForumTopicReopened.type]
  implicit val forumTopicEditedDecoder: Decoder[ForumTopicEdited]          = deriveDecoder[ForumTopicEdited]

  implicit val createForumTopicDecoder: Decoder[CreateForumTopic] = deriveDecoder[CreateForumTopic]
  implicit val editForumTopicDecoder: Decoder[EditForumTopic]     = deriveDecoder[EditForumTopic]
  implicit val closeForumTopicDecoder: Decoder[CloseForumTopic]   = deriveDecoder[CloseForumTopic]
  implicit val reopenForumTopicDecoder: Decoder[ReopenForumTopic] = deriveDecoder[ReopenForumTopic]
  implicit val deleteForumTopicDecoder: Decoder[DeleteForumTopic] = deriveDecoder[DeleteForumTopic]
  implicit val unpinAllForumTopicMessagesDecoder: Decoder[UnpinAllForumTopicMessages] =
    deriveDecoder[UnpinAllForumTopicMessages]
  implicit val getForumTopicIconStickersDecoder: Decoder[GetForumTopicIconStickers.type] =
    deriveDecoder[GetForumTopicIconStickers.type]

  // for v6.0 support
  implicit val webAppInfoDecoder: Decoder[WebAppInfo]                   = deriveDecoder[WebAppInfo]
  implicit val webAppDataDecoder: Decoder[WebAppData]                   = deriveDecoder[WebAppData]
  implicit val chatAdminRightsDecoder: Decoder[ChatAdministratorRights] = deriveDecoder[ChatAdministratorRights]
  implicit val sentWebAppMessageDecoder: Decoder[SentWebAppMessage]     = deriveDecoder[SentWebAppMessage]
  // for v5.1 support
  implicit val chatInviteLinkDecoder: Decoder[ChatInviteLink]       = deriveDecoder[ChatInviteLink]
  implicit val chatMemberUpdatedDecoder: Decoder[ChatMemberUpdated] = deriveDecoder[ChatMemberUpdated]

  implicit val chatJoinrequestDecoder: Decoder[ChatJoinRequest] = deriveDecoder[ChatJoinRequest]

  implicit val audioDecoder: Decoder[Audio] = deriveDecoder[Audio]

  implicit val chatDecoder: Decoder[Chat]           = deriveDecoder[Chat]
  implicit val chatPhotoDecoder: Decoder[ChatPhoto] = deriveDecoder[ChatPhoto]

  implicit val KeyboardButtonPollTypeDecoder: Decoder[KeyboardButtonPollType] = deriveDecoder[KeyboardButtonPollType]

  implicit val menuButtonDecoder: Decoder[MenuButton]                     = MenuButton.menuButtonDecoder
  implicit val contactDecoder: Decoder[Contact]                           = deriveDecoder[Contact]
  implicit val documentDecoder: Decoder[Document]                         = deriveDecoder[Document]
  implicit val fileDecoder: Decoder[File]                                 = deriveDecoder[File]
  implicit val callbackGameDecoder: Decoder[CallbackGame]                 = Decoder.const(CallbackGame)
  implicit val inlineKeyboardButtonDecoder: Decoder[InlineKeyboardButton] = deriveDecoder[InlineKeyboardButton]

  implicit val inlineKeyboardMarkupDecoder: Decoder[InlineKeyboardMarkup] = deriveDecoder[InlineKeyboardMarkup]

  implicit val keyboardButtonDecoder: Decoder[KeyboardButton] = deriveDecoder[KeyboardButton]
  implicit val locationDecoder: Decoder[Location]             = deriveDecoder[Location]

  implicit val messageEntityDecoder: Decoder[MessageEntity] = deriveDecoder[MessageEntity]

  implicit val webhookInfoDecoder: Decoder[WebhookInfo] = deriveDecoder[WebhookInfo]

  implicit val photoSizeDecoder: Decoder[PhotoSize] = deriveDecoder[PhotoSize]

  implicit val replyMarkupDecoder: Decoder[ReplyMarkup] = deriveDecoder[ReplyMarkup]

  implicit val listStickerDecoder: Decoder[List[Sticker]] = deriveDecoder[List[Sticker]]
  implicit val stickerDecoder: Decoder[Sticker]           = deriveDecoder[Sticker]

  implicit val eitherMessageBooleanDecoder: Decoder[Either[Message, Boolean]] = deriveDecoder[Either[Message, Boolean]]
  implicit val eitherBooleanMessageDecoder: Decoder[Either[Boolean, Message]] = deriveDecoder[Either[Boolean, Message]]
  implicit val arrayMessageDecoder: Decoder[Array[Message]]                   = Decoder.decodeList[Message].map(_.toArray)
  implicit val messageDecoder: Decoder[Message]                               = deriveDecoder[Message]
  implicit val messageIdDecoder: Decoder[MessageId]                           = deriveDecoder[MessageId]
  implicit val callbackQueryDecoder: Decoder[CallbackQuery]                   = deriveDecoder[CallbackQuery]

  implicit val stickerSetDecoder: Decoder[StickerSet] = deriveDecoder[StickerSet]

  implicit val seqChatMemberDecoder: Decoder[Seq[ChatMember]] = Decoder.decodeSeq[ChatMember].map(_.toSeq)
  implicit val chatMemberDecoder: Decoder[ChatMember]         = deriveDecoder[ChatMember]

  implicit val chatPermissionsDecoder: Decoder[ChatPermissions] = deriveDecoder[ChatPermissions]

  implicit val maskPositionDecoder: Decoder[MaskPosition] = deriveDecoder[MaskPosition]

  implicit val userDecoder: Decoder[User]                           = deriveDecoder[User]
  implicit val userProfilePhotosDecoder: Decoder[UserProfilePhotos] = deriveDecoder[UserProfilePhotos]
  implicit val venueDecoder: Decoder[Venue]                         = deriveDecoder[Venue]
  implicit val videoDecoder: Decoder[Video]                         = deriveDecoder[Video]
  implicit val storyDecoder: Decoder[Story.type]                    = deriveDecoder[Story.type]
  implicit val videoNoteDecoder: Decoder[VideoNote]                 = deriveDecoder[VideoNote]
  implicit val voiceDecoder: Decoder[Voice]                         = deriveDecoder[Voice]
  implicit val videoChatEndedDecoder: Decoder[VideoChatEnded]       = deriveDecoder[VideoChatEnded]
  implicit val videoChatParticipantsInvitedDecoder: Decoder[VideoChatParticipantsInvited] =
    deriveDecoder[VideoChatParticipantsInvited]
  implicit val videoChatScheduledDecoder: Decoder[VideoChatScheduled]  = deriveDecoder[VideoChatScheduled]
  implicit val videoChatStartedDecoder: Decoder[VideoChatStarted.type] = deriveDecoder[VideoChatStarted.type]

  implicit val seqGameHighScoreDecoder: Decoder[Seq[GameHighScore]] = Decoder.decodeSeq[GameHighScore].map(_.toSeq)
  implicit val gameHighScoreDecoder: Decoder[GameHighScore]         = deriveDecoder[GameHighScore]
  implicit val animationDecoder: Decoder[Animation]                 = deriveDecoder[Animation]
  implicit val gameDecoder: Decoder[Game]                           = deriveDecoder[Game]

  implicit val inlineQueryDecoder: Decoder[InlineQuery]              = deriveDecoder[InlineQuery]
  implicit val chosenInlineQueryDecoder: Decoder[ChosenInlineResult] = deriveDecoder[ChosenInlineResult]

  implicit val inputContactMessageContent: Decoder[InputContactMessageContent] =
    deriveDecoder[InputContactMessageContent]
  implicit val inputVenueMessageContentDecoder: Decoder[InputVenueMessageContent] =
    deriveDecoder[InputVenueMessageContent]
  implicit val inputLocationMessageContentDecoder: Decoder[InputLocationMessageContent] =
    deriveDecoder[InputLocationMessageContent]
  implicit val inputTextMessageContentDecoder: Decoder[InputTextMessageContent] =
    deriveDecoder[InputTextMessageContent]

  implicit val labeledPriceDecoder: Decoder[LabeledPrice]       = deriveDecoder[LabeledPrice]
  implicit val invoiceDecoder: Decoder[Invoice]                 = deriveDecoder[Invoice]
  implicit val shippingAddressDecoder: Decoder[ShippingAddress] = deriveDecoder[ShippingAddress]

  implicit val pollDecoder: Decoder[Poll]             = deriveDecoder[Poll]
  implicit val pollOptionDecoder: Decoder[PollOption] = deriveDecoder[PollOption]

  implicit val shippingQueryDecoder: Decoder[ShippingQuery]         = deriveDecoder[ShippingQuery]
  implicit val orderInfoDecoder: Decoder[OrderInfo]                 = deriveDecoder[OrderInfo]
  implicit val preCheckoutQueryDecoder: Decoder[PreCheckoutQuery]   = deriveDecoder[PreCheckoutQuery]
  implicit val shippingOptionDecoder: Decoder[ShippingOption]       = deriveDecoder[ShippingOption]
  implicit val successfulPaymentDecoder: Decoder[SuccessfulPayment] = deriveDecoder[SuccessfulPayment]

  implicit val responseParametersDecoder: Decoder[ResponseParameters] = deriveDecoder[ResponseParameters]

  implicit val updateDecoder: Decoder[Update] = deriveDecoder[Update]

  implicit val seqParsedUpdateDecoder: Decoder[Seq[ParsedUpdate]] = Decoder.decodeSeq[ParsedUpdate].map(_.toSeq)
  implicit val parsedUpdateDecoder: Decoder[ParsedUpdate] = new Decoder[ParsedUpdate] {
    final def apply(c: HCursor): Decoder.Result[ParsedUpdate] = {
      val update = updateDecoder(c)

      update match {
        case Left(e) =>
          for {
            id <- c.get[Long]("updateId")
          } yield ParsedUpdate.Failure(id, e)
        case Right(value) => Right(ParsedUpdate.Success(value))
      }
    }
  }

  implicit val loginUrlDecoder: Decoder[LoginUrl] = deriveDecoder[LoginUrl]

  implicit def responseDecoder[T: Decoder]: Decoder[Response[T]] = deriveDecoder[Response[T]]
}

object CirceDecoders extends CirceDecoders
